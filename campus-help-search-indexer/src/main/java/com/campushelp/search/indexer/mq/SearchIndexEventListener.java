package com.campushelp.search.indexer.mq;

import com.campushelp.common.event.EventBusConstants;
import com.campushelp.common.event.SearchIndexEvent;
import com.campushelp.search.indexer.service.SearchIndexingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SearchIndexEventListener {
    private final ObjectMapper objectMapper;
    private final SearchIndexingService searchIndexingService;
    private final StringRedisTemplate redisTemplate;

    public SearchIndexEventListener(ObjectMapper objectMapper,
                                    SearchIndexingService searchIndexingService,
                                    StringRedisTemplate redisTemplate) {
        this.objectMapper = objectMapper;
        this.searchIndexingService = searchIndexingService;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = EventBusConstants.SEARCH_INDEX_QUEUE, containerFactory = "manualAckRabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            SearchIndexEvent event = objectMapper.readValue(message.getBody(), SearchIndexEvent.class);
            if (isHandled(event.getEventId())) {
                channel.basicAck(tag, false);
                return;
            }
            if ("DELETE".equalsIgnoreCase(event.getOp())) {
                searchIndexingService.delete(event);
            } else {
                searchIndexingService.upsert(event);
            }
            markHandled(event.getEventId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicReject(tag, false);
        }
    }

    private boolean isHandled(String eventId) {
        if (eventId == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey("search:index:idempotent:" + eventId));
    }

    private void markHandled(String eventId) {
        if (eventId == null) {
            return;
        }
        redisTemplate.opsForValue().set("search:index:idempotent:" + eventId, "1", Duration.ofHours(24));
    }
}
