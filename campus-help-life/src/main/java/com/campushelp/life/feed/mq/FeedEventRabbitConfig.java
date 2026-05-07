package com.campushelp.life.feed.mq;

import com.campushelp.common.event.EventBusConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FeedEventRabbitConfig {

    @Bean
    @ConditionalOnMissingBean(name = "campusEventExchange")
    public TopicExchange campusEventExchange() {
        return new TopicExchange(EventBusConstants.EVENT_EXCHANGE, true, false);
    }

    @Bean
    @ConditionalOnMissingBean(name = "campusEventDlxExchange")
    public DirectExchange campusEventDlxExchange() {
        return new DirectExchange(EventBusConstants.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue campusEventFeedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EventBusConstants.DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", EventBusConstants.FEED_DLX_ROUTING_KEY);
        return new Queue(EventBusConstants.FEED_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue campusEventFeedDlq() {
        return new Queue(EventBusConstants.FEED_DLQ, true);
    }

    @Bean
    public Binding campusEventFeedBinding(TopicExchange campusEventExchange, Queue campusEventFeedQueue) {
        return BindingBuilder.bind(campusEventFeedQueue).to(campusEventExchange).with(EventBusConstants.EVENT_ROUTING_KEY);
    }

    @Bean
    public Binding campusEventFeedDlqBinding(DirectExchange campusEventDlxExchange, Queue campusEventFeedDlq) {
        return BindingBuilder.bind(campusEventFeedDlq).to(campusEventDlxExchange).with(EventBusConstants.FEED_DLX_ROUTING_KEY);
    }
}
