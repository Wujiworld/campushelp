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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Component
public class CacheFacade {
    private static final String NULL_MARKER = "__NULL__";

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
        this.localCache = Caffeine.newBuilder()
                .maximumSize(properties.getLocalMaximumSize())
                .expireAfterWrite(Duration.ofSeconds(properties.getLocalExpireSeconds()))
                .build();
        this.hitLocal = meterRegistry.counter("campus.cache.hit", "layer", "caffeine");
        this.hitRedis = meterRegistry.counter("campus.cache.hit", "layer", "redis");
        this.hitDb = meterRegistry.counter("campus.cache.hit", "layer", "source");
    }

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
        String lockKey = properties.getLockPrefix() + key;
        boolean locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5)));
        if (!locked) {
            String retry = redisTemplate.opsForValue().get(key);
            if (retry != null) {
                localCache.put(key, retry);
                hitRedis.increment();
                return decode(retry, type);
            }
            return null;
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
        String lockKey = properties.getLockPrefix() + key;
        boolean locked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5)));
        if (!locked) {
            String retry = redisTemplate.opsForValue().get(key);
            if (retry != null) {
                localCache.put(key, retry);
                hitRedis.increment();
                return NULL_MARKER.equals(retry) ? null : retry;
            }
            return null;
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

    private void store(String key, String value, long ttlSeconds) {
        long jitter = properties.getJitterSeconds() <= 0
                ? 0
                : ThreadLocalRandom.current().nextLong(properties.getJitterSeconds() + 1);
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds + jitter));
        localCache.put(key, value);
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
