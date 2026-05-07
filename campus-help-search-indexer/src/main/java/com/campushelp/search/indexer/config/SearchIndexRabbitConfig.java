package com.campushelp.search.indexer.config;

import com.campushelp.common.event.EventBusConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SearchIndexRabbitConfig {
    @Bean
    public TopicExchange searchTopicExchange() {
        return new TopicExchange(EventBusConstants.EVENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange searchDlxExchange() {
        return new DirectExchange(EventBusConstants.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue searchIndexQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EventBusConstants.DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", EventBusConstants.SEARCH_INDEX_DLX_ROUTING_KEY);
        return new Queue(EventBusConstants.SEARCH_INDEX_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue searchIndexDlq() {
        return new Queue(EventBusConstants.SEARCH_INDEX_DLQ, true);
    }

    @Bean
    public Binding searchIndexBinding(Queue searchIndexQueue, TopicExchange searchTopicExchange) {
        return BindingBuilder.bind(searchIndexQueue).to(searchTopicExchange).with(EventBusConstants.SEARCH_INDEX_ROUTING_KEY);
    }

    @Bean
    public Binding searchIndexDlqBinding(Queue searchIndexDlq, DirectExchange searchDlxExchange) {
        return BindingBuilder.bind(searchIndexDlq).to(searchDlxExchange).with(EventBusConstants.SEARCH_INDEX_DLX_ROUTING_KEY);
    }
}
