package com.campushelp.life.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "campus.seckill.enabled", havingValue = "true")
public class SeckillRabbitConfig {

    public static final String EXCHANGE = "campus.seckill.topic";
    public static final String QUEUE = "seckill.order.create";
    public static final String ROUTING_KEY = "order.create";

    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding seckillBinding(Queue seckillOrderQueue, TopicExchange seckillExchange) {
        return BindingBuilder.bind(seckillOrderQueue).to(seckillExchange).with(ROUTING_KEY);
    }
}
