package com.campushelp.server.event;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.DomainEventPublisher;
import com.campushelp.order.entity.ChOutbox;
import com.campushelp.order.mapper.ChOutboxMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 有事务：事件写入 Outbox（与同事务提交），由 {@link OutboxPublishScheduler} 投递 MQ。
 * 无事务：直接发 MQ（兼容非事务调用方）。
 */
@Component
public class RabbitDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitDomainEventPublisher.class);

    private final ChOutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;
    private final AmqpDomainEventSender amqpDomainEventSender;

    public RabbitDomainEventPublisher(ChOutboxMapper outboxMapper,
                                      ObjectMapper objectMapper,
                                      AmqpDomainEventSender amqpDomainEventSender) {
        this.outboxMapper = outboxMapper;
        this.objectMapper = objectMapper;
        this.amqpDomainEventSender = amqpDomainEventSender;
    }

    @Override
    public void publishAfterCommit(DomainEvent event) {
        if (event == null) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            insertOutbox(event);
            return;
        }
        amqpDomainEventSender.send(event);
    }

    private void insertOutbox(DomainEvent event) {
        try {
            ChOutbox row = new ChOutbox();
            row.setEventId(event.getEventId());
            row.setPayloadJson(objectMapper.writeValueAsString(event));
            row.setCreatedAt(LocalDateTime.now());
            outboxMapper.insert(row);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("outbox serialize failed", e);
        } catch (DuplicateKeyException e) {
            log.debug("Outbox duplicate eventId={}, skip", event.getEventId());
        }
    }
}
