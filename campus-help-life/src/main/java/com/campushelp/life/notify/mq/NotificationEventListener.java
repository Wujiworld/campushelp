package com.campushelp.life.notify.mq;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.EventBusConstants;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.life.client.OrderNotifyClient;
import com.campushelp.life.notify.mq.support.NotificationMessageComposer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationEventListener {

    private final ObjectMapper objectMapper;
    private final OrderNotifyClient orderNotifyClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMessageComposer composer;

    public NotificationEventListener(ObjectMapper objectMapper,
                                     OrderNotifyClient orderNotifyClient,
                                     SimpMessagingTemplate messagingTemplate,
                                     NotificationMessageComposer composer) {
        this.objectMapper = objectMapper;
        this.orderNotifyClient = orderNotifyClient;
        this.messagingTemplate = messagingTemplate;
        this.composer = composer;
    }

    @RabbitListener(queues = EventBusConstants.NOTIFY_QUEUE, containerFactory = "manualAckRabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        DomainEvent ev;
        try {
            byte[] body = message.getBody();
            ev = objectMapper.readValue(body, DomainEvent.class);
        } catch (Exception e) {
            channel.basicReject(tag, false);
            return;
        }

        try {
            handle(ev);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            // 进入 DLQ（由队列 DLX 配置接管）
            channel.basicReject(tag, false);
        }
    }

    public void handle(DomainEvent ev) {
        if (ev == null || ev.getEventId() == null || ev.getType() == null) {
            return;
        }
        NotificationMessageComposer.Composed composed = composer.compose(ev);
        List<Long> recipients = ev.getRecipients() == null ? null : Arrays.stream(ev.getRecipients())
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
        orderNotifyClient.upsertEventMessage(
                ev.getEventId(),
                ev.getType().name(),
                ev.getBizId(),
                StringUtils.hasText(composed.getTitle()) ? composed.getTitle() : ev.getType().name(),
                StringUtils.hasText(composed.getContent()) ? composed.getContent() : "",
                composed.getPayloadJson(),
                recipients);

        push(ev);
    }

    private void push(DomainEvent ev) {
        if (ev.getType() == NotificationEventType.SYSTEM_ANNOUNCEMENT) {
            messagingTemplate.convertAndSend("/topic/broadcast", ev);
            return;
        }
        if (ev.getRecipients() == null) {
            return;
        }
        for (Long uid : ev.getRecipients()) {
            if (uid == null) continue;
            messagingTemplate.convertAndSend("/topic/user." + uid, ev);
        }
    }
}

