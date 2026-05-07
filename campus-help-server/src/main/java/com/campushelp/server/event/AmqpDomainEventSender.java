package com.campushelp.server.event;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.EventBusConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class AmqpDomainEventSender {

    private static final Logger log = LoggerFactory.getLogger(AmqpDomainEventSender.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public AmqpDomainEventSender(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(DomainEvent event) {
        if (event == null) {
            return;
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            props.setContentEncoding(StandardCharsets.UTF_8.name());
            Message msg = new Message(body, props);
            rabbitTemplate.send(EventBusConstants.EVENT_EXCHANGE, EventBusConstants.EVENT_ROUTING_KEY, msg);
        } catch (Exception e) {
            log.warn("Domain event AMQP send failed, eventId={}", event.getEventId(), e);
        }
    }
}
