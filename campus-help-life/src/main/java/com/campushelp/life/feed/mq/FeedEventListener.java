package com.campushelp.life.feed.mq;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.EventBusConstants;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.life.feed.dto.FeedItemDTO;
import com.campushelp.life.feed.service.FeedService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FeedEventListener {
    private final ObjectMapper objectMapper;
    private final FeedService feedService;

    public FeedEventListener(ObjectMapper objectMapper, FeedService feedService) {
        this.objectMapper = objectMapper;
        this.feedService = feedService;
    }

    @RabbitListener(queues = EventBusConstants.FEED_QUEUE, containerFactory = "manualAckRabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            DomainEvent event = objectMapper.readValue(message.getBody(), DomainEvent.class);
            if (event != null && event.getType() == NotificationEventType.FEED_POST_PUBLISHED) {
                FeedItemDTO item = feedService.fromEventPayload(event.getPayload());
                feedService.fanoutToFollowers(item);
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicReject(tag, false);
        }
    }
}
