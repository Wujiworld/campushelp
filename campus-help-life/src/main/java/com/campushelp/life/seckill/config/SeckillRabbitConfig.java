package com.campushelp.life.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "campus.seckill.enabled", havingValue = "true")
public class SeckillRabbitConfig {

    public static final String EXCHANGE = "campus.seckill.topic";
    public static final String QUEUE = "seckill.order.create";
    public static final String ROUTING_KEY = "order.create";

    public static final String DLX = "campus.seckill.dlx";
    public static final String DLQ = "seckill.order.create.dlq";
    public static final String DLX_ROUTING_KEY = "order.create.dlq";

    public static final String RETRY_HEADER = "x-retry-count";
    public static final int MAX_RETRY = 3;

    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange seckillDlx() {
        return new TopicExchange(DLX, true, false);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue seckillDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding seckillBinding(Queue seckillOrderQueue, TopicExchange seckillExchange) {
        return BindingBuilder.bind(seckillOrderQueue).to(seckillExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding seckillDlqBinding(Queue seckillDlq, TopicExchange seckillDlx) {
        return BindingBuilder.bind(seckillDlq).to(seckillDlx).with(DLX_ROUTING_KEY);
    }
}
