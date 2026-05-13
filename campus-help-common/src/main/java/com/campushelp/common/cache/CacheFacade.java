package com.campushelp.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Component
public class CacheFacade {
    private static final String NULL_MARKER = "__NULL__";
    private static final int SPIN_RETRIES = 4;
    private static final long SPIN_INTERVAL_MS = 50;

    private final Cache<String, String> localCache;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CampusCacheProperties properties;
    private final Counter hitLocal;
    private final Counter hitRedis;
    private final Counter hitDb;

    public CacheFacade(StringRedisTemplate redisTemplate,
                       ObjectMapper objectMapper,
                       CampusCacheProperties properties,
                       MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        // Caffeine TTL 在构建时加入随机偏移，分散本地缓存过期时间
        long localTtl = properties.getLocalExpireSeconds()
                + randomJitter(properties.getJitterSeconds());
        this.localCache = Caffeine.newBuilder()
                .maximumSize(properties.getLocalMaximumSize())
                .expireAfterWrite(Duration.ofSeconds(localTtl))
                .build();
        this.hitLocal = meterRegistry.counter("campus.cache.hit", "layer", "caffeine");
        this.hitRedis = meterRegistry.counter("campus.cache.hit", "layer", "redis");
        this.hitDb = meterRegistry.counter("campus.cache.hit", "layer", "source");
    }

    // ==================== 泛型读接口 ====================

    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        String local = localCache.getIfPresent(key);
        if (local != null) {
            hitLocal.increment();
            return decode(local, type);
        }
        String remote = redisTemplate.opsForValue().get(key);
        if (remote != null) {
            localCache.put(key, remote);
            hitRedis.increment();
            return decode(remote, type);
        }
        return loadWithMutex(key, type, loader);
    }

    public String getOrLoadJson(String key, Supplier<String> loader) {
        String local = localCache.getIfPresent(key);
        if (local != null) {
            hitLocal.increment();
            return NULL_MARKER.equals(local) ? null : local;
        }
        String remote = redisTemplate.opsForValue().get(key);
        if (remote != null) {
            localCache.put(key, remote);
            hitRedis.increment();
            return NULL_MARKER.equals(remote) ? null : remote;
        }
        return loadJsonWithMutex(key, loader);
    }

    // ==================== 互斥重建（防击穿） ====================

    private <T> T loadWithMutex(String key, Class<T> type, Supplier<T> loader) {
        String lockKey = properties.getLockPrefix() + key;
        boolean locked = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5)));
        if (!locked) {
            // 自旋等待：持有锁的线程正在查 DB，最多等 ~200ms
            return spinWait(key, type);
        }
        try {
            T loaded = loader.get();
            hitDb.increment();
            if (loaded == null) {
                store(key, NULL_MARKER, properties.getNullTtlSeconds());
                return null;
            }
            String json = objectMapper.writeValueAsString(loaded);
            store(key, json, properties.getDefaultTtlSeconds());
            return loaded;
        } catch (Exception e) {
            throw new IllegalStateException("cache load failed", e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private String loadJsonWithMutex(String key, Supplier<String> loader) {
        String lockKey = properties.getLockPrefix() + key;
        boolean locked = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5)));
        if (!locked) {
            return spinWaitJson(key);
        }
        try {
            String loaded = loader.get();
            hitDb.increment();
            if (!StringUtils.hasText(loaded)) {
                store(key, NULL_MARKER, properties.getNullTtlSeconds());
                return null;
            }
            store(key, loaded, properties.getDefaultTtlSeconds());
            return loaded;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private <T> T spinWait(String key, Class<T> type) {
        for (int i = 0; i < SPIN_RETRIES; i++) {
            try { Thread.sleep(SPIN_INTERVAL_MS); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); break;
            }
            String retry = redisTemplate.opsForValue().get(key);
            if (retry != null) {
                localCache.put(key, retry);
                hitRedis.increment();
                return decode(retry, type);
            }
            // 锁已释放但还没数据 → 加载线程可能失败了，提前退出
            if (Boolean.FALSE.equals(redisTemplate.hasKey(properties.getLockPrefix() + key))) {
                break;
            }
        }
        return null;
    }

    private String spinWaitJson(String key) {
        for (int i = 0; i < SPIN_RETRIES; i++) {
            try { Thread.sleep(SPIN_INTERVAL_MS); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); break;
            }
            String retry = redisTemplate.opsForValue().get(key);
            if (retry != null) {
                localCache.put(key, retry);
                hitRedis.increment();
                return NULL_MARKER.equals(retry) ? null : retry;
            }
            if (Boolean.FALSE.equals(redisTemplate.hasKey(properties.getLockPrefix() + key))) {
                break;
            }
        }
        return null;
    }

    // ==================== 主动失效 ====================

    /**
     * 失效单个缓存键（本地 + Redis）。
     */
    public void evict(String key) {
        if (key == null) return;
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }

    /**
     * 按前缀批量失效 Redis 缓存键（本地缓存靠 TTL 自然过期）。
     */
    public void evictByPrefix(String prefix) {
        if (prefix == null) return;
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // ==================== 写入（防雪崩 + 防穿透） ====================

    private void store(String key, String value, long ttlSeconds) {
        long jitter = randomJitter(properties.getJitterSeconds());
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds + jitter));
        localCache.put(key, value);
    }

    // ==================== 工具 ====================

    private static long randomJitter(long maxJitter) {
        return maxJitter <= 0 ? 0 : ThreadLocalRandom.current().nextLong(maxJitter + 1);
    }

    private <T> T decode(String value, Class<T> type) {
        if (!StringUtils.hasText(value) || NULL_MARKER.equals(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            throw new IllegalStateException("cache decode failed", e);
        }
    }
}
