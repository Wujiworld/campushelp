package com.campushelp.life.seckill.listener;

import com.campushelp.life.seckill.config.SeckillRabbitConfig;
import com.campushelp.life.client.OrderServiceClient;
import com.campushelp.life.seckill.dto.SeckillTicketMessage;
import com.campushelp.life.seckill.service.SeckillTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "campus.seckill.enabled", havingValue = "true")
@ConditionalOnBean(RabbitTemplate.class)
public class SeckillOrderListener {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderListener.class);

    private final OrderServiceClient orderServiceClient;
    private final SeckillTicketService seckillTicketService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${campus.seckill.idempotency-key-prefix:seckill:idempotency:}")
    private String idempotencyKeyPrefix;

    @Value("${campus.seckill.idempotency-ttl-hours:24}")
    private long idempotencyTtlHours;

    public SeckillOrderListener(OrderServiceClient orderServiceClient,
                                SeckillTicketService seckillTicketService,
                                StringRedisTemplate stringRedisTemplate) {
        this.orderServiceClient = orderServiceClient;
        this.seckillTicketService = seckillTicketService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @RabbitListener(queues = SeckillRabbitConfig.QUEUE)
    public void onMessage(SeckillTicketMessage msg) {
        if (msg.getIdempotencyKey() != null && isDuplicate(msg.getIdempotencyKey())) {
            log.info("Seckill message duplicate, idempotencyKey={}, skip", msg.getIdempotencyKey());
            return;
        }
        try {
            orderServiceClient.createTicketOrder(msg.getUserId(), msg.getCampusId(), msg.getTicketTypeId());
            if (msg.getIdempotencyKey() != null) {
                markProcessed(msg.getIdempotencyKey());
            }
        } catch (RuntimeException e) {
            log.warn("Seckill order create failed, idempotencyKey={}, ticketTypeId={}",
                    msg.getIdempotencyKey(), msg.getTicketTypeId(), e);
            seckillTicketService.compensateStock(msg.getTicketTypeId());
            throw e;
        }
    }

    @RabbitListener(queues = SeckillRabbitConfig.DLQ)
    public void onDeadLetter(SeckillTicketMessage msg, Message raw) {
        log.error("Seckill order DLQ received, idempotencyKey={}, userId={}, ticketTypeId={}, redelivered={}",
                msg.getIdempotencyKey(), msg.getUserId(), msg.getTicketTypeId(),
                raw.getMessageProperties().getRedelivered());
        // Stock already compensated by main listener; DLQ entry alerts ops for manual review
    }

    private boolean isDuplicate(String idempotencyKey) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(idempotencyKeyPrefix + idempotencyKey));
    }

    private void markProcessed(String idempotencyKey) {
        stringRedisTemplate.opsForValue().set(
                idempotencyKeyPrefix + idempotencyKey, "1", idempotencyTtlHours, TimeUnit.HOURS);
    }
}
