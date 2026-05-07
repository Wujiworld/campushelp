package com.campushelp.server.event;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.common.event.DomainEvent;
import com.campushelp.order.entity.ChOutbox;
import com.campushelp.order.mapper.ChOutboxMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫描 Outbox 未投递记录并发送到 RabbitMQ（与事务内写入配合，避免仅 afterCommit 时 MQ 故障丢消息）。
 */
@Component
@ConditionalOnBean(ChOutboxMapper.class)
@ConditionalOnProperty(name = "campus.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublishScheduler.class);

    private final ChOutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;
    private final AmqpDomainEventSender amqpDomainEventSender;

    public OutboxPublishScheduler(ChOutboxMapper outboxMapper,
                                  ObjectMapper objectMapper,
                                  AmqpDomainEventSender amqpDomainEventSender) {
        this.outboxMapper = outboxMapper;
        this.objectMapper = objectMapper;
        this.amqpDomainEventSender = amqpDomainEventSender;
    }

    @Scheduled(fixedDelayString = "${campus.outbox.publish-ms:1000}")
    public void publishPending() {
        List<ChOutbox> pending = outboxMapper.selectList(
                new QueryWrapper<ChOutbox>()
                        .isNull("published_at")
                        .orderByAsc("id")
                        .last("LIMIT 100"));
        for (ChOutbox row : pending) {
            try {
                DomainEvent ev = objectMapper.readValue(row.getPayloadJson(), DomainEvent.class);
                amqpDomainEventSender.send(ev);
                ChOutbox patch = new ChOutbox();
                patch.setId(row.getId());
                patch.setPublishedAt(LocalDateTime.now());
                outboxMapper.updateById(patch);
            } catch (Exception e) {
                log.warn("Outbox row publish failed, id={}, eventId={}", row.getId(), row.getEventId(), e);
            }
        }
    }
}
