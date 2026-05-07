package com.campushelp.life.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 延迟关单：x-delayed-message 插件交换机（未装插件时可改用 TTL+DLX，见运维文档）。
 */
@Configuration
@ConditionalOnProperty(name = "campus.mq.order-delay.enabled", havingValue = "true")
@ConditionalOnBean(RabbitTemplate.class)
public class OrderDelayRabbitConfig {

    public static final String EXCHANGE_DELAYED = "campus.order.delayed";
    public static final String QUEUE_CLOSE = "order.close.unpaid";
    public static final String ROUTING_KEY = "order.close";

    @Bean
    public CustomExchange orderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(EXCHANGE_DELAYED, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue orderCloseQueue() {
        return new Queue(QUEUE_CLOSE, true);
    }

    @Bean
    public Binding orderCloseBinding(Queue orderCloseQueue, CustomExchange orderDelayExchange) {
        return BindingBuilder.bind(orderCloseQueue).to(orderDelayExchange).with(ROUTING_KEY).noargs();
    }
}
