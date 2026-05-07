package com.campushelp.life.seckill.service;

import com.campushelp.common.api.ResultCode;
import com.campushelp.common.exception.BusinessException;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.dto.TicketTypeDto;
import com.campushelp.life.seckill.config.SeckillRabbitConfig;
import com.campushelp.life.seckill.dto.SeckillTicketMessage;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "campus.seckill.enabled", havingValue = "true")
@ConditionalOnBean({StringRedisTemplate.class, RedissonClient.class, RabbitTemplate.class})
public class SeckillTicketService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final LifeCatalogClient lifeCatalogClient;

    @Value("${campus.seckill.stock-key-prefix:seckill:stock:}")
    private String stockKeyPrefix;

    @Value("${campus.seckill.user-limit-key-prefix:seckill:user:}")
    private String userLimitKeyPrefix;

    @Value("${campus.seckill.default-user-limit:1}")
    private int defaultUserLimit;

    private final DefaultRedisScript<Long> decrScript = buildScript();

    public SeckillTicketService(StringRedisTemplate stringRedisTemplate,
                                RedissonClient redissonClient,
                                RabbitTemplate rabbitTemplate,
                                LifeCatalogClient lifeCatalogClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.rabbitTemplate = rabbitTemplate;
        this.lifeCatalogClient = lifeCatalogClient;
    }

    private static DefaultRedisScript<Long> buildScript() {
        DefaultRedisScript<Long> s = new DefaultRedisScript<>();
        s.setResultType(Long.class);
        s.setScriptText(
                "local v = redis.call('GET', KEYS[1])\n"
                        + "if (not v or tonumber(v) < 1) then return -1 end\n"
                        + "return redis.call('DECR', KEYS[1])");
        return s;
    }

    /** 与 DB 对齐：启动或管理端可调用 */
    public void warmupStock(long ticketTypeId) {
        TicketTypeDto tt = lifeCatalogClient.getTicket(ticketTypeId);
        if (tt == null || !"ON".equals(tt.getStatus())) {
            return;
        }
        int left = tt.getStockTotal() - tt.getStockSold();
        if (left < 0) {
            left = 0;
        }
        stringRedisTemplate.opsForValue().set(stockKeyPrefix + ticketTypeId, String.valueOf(left));
    }

    public Map<String, Object> submit(long userId, long campusId, long ticketTypeId) {
        String lockKey = "seckill:lock:user:" + userId + ":ticket:" + ticketTypeId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(ResultCode.BIZ_RULE, "排队超时，请重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.BIZ_RULE, "排队被中断");
        }
        try {
            String sk = stockKeyPrefix + ticketTypeId;
            Long r = stringRedisTemplate.execute(decrScript, Collections.singletonList(sk));
            if (r == null || r < 0) {
                if (r != null && r < 0) {
                    stringRedisTemplate.opsForValue().increment(sk);
                }
                throw new BusinessException(ResultCode.BIZ_RULE, "已抢完");
            }
            boolean reserved = reserveUserQuota(userId, ticketTypeId);
            if (!reserved) {
                stringRedisTemplate.opsForValue().increment(sk);
                throw new BusinessException(ResultCode.BIZ_RULE, "超过单用户限购");
            }
            String idem = userId + ":" + ticketTypeId + ":" + UUID.randomUUID();
            SeckillTicketMessage msg = new SeckillTicketMessage(userId, campusId, ticketTypeId, idem);
            rabbitTemplate.convertAndSend(SeckillRabbitConfig.EXCHANGE, SeckillRabbitConfig.ROUTING_KEY, msg);
            Map<String, Object> m = new HashMap<>();
            m.put("accepted", true);
            m.put("ticketTypeId", ticketTypeId);
            m.put("idempotencyKey", idem);
            return m;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void compensateStock(long ticketTypeId) {
        stringRedisTemplate.opsForValue().increment(stockKeyPrefix + ticketTypeId);
        lifeCatalogClient.releaseTicketStock(ticketTypeId, 1);
    }

    private boolean reserveUserQuota(long userId, long ticketTypeId) {
        String userKey = userLimitKeyPrefix + userId + ":" + ticketTypeId;
        Long n = stringRedisTemplate.opsForValue().increment(userKey);
        if (n != null && n == 1L) {
            stringRedisTemplate.expire(userKey, 1, TimeUnit.DAYS);
        }
        if (n != null && n > defaultUserLimit) {
            stringRedisTemplate.opsForValue().decrement(userKey);
            return false;
        }
        return true;
    }
}
