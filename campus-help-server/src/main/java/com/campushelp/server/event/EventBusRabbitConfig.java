package com.campushelp.server.event;

import com.campushelp.common.event.EventBusConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.AcknowledgeMode;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EventBusRabbitConfig {

    @Bean
    public TopicExchange campusEventExchange() {
        return new TopicExchange(EventBusConstants.EVENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange campusEventDlxExchange() {
        return new DirectExchange(EventBusConstants.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue campusEventNotifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EventBusConstants.DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", EventBusConstants.DLX_ROUTING_KEY);
        return new Queue(EventBusConstants.NOTIFY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue campusEventNotifyDlq() {
        return new Queue(EventBusConstants.DLQ, true);
    }

    @Bean
    public Binding campusEventNotifyBinding(TopicExchange campusEventExchange, Queue campusEventNotifyQueue) {
        return BindingBuilder.bind(campusEventNotifyQueue).to(campusEventExchange).with(EventBusConstants.EVENT_ROUTING_KEY);
    }

    @Bean
    public Binding campusEventNotifyDlqBinding(DirectExchange campusEventDlxExchange, Queue campusEventNotifyDlq) {
        return BindingBuilder.bind(campusEventNotifyDlq).to(campusEventDlxExchange).with(EventBusConstants.DLX_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        // 发布端已发送 JSON bytes；consumer 自行反序列化
        return new SimpleMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate t = new RabbitTemplate(connectionFactory);
        t.setMessageConverter(rabbitMessageConverter);
        return t;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory manualAckRabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                                        Environment environment) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(connectionFactory);
        f.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        f.setPrefetchCount(50);
        boolean autoStartup = environment.getProperty("spring.rabbitmq.listener.simple.auto-startup", Boolean.class, true);
        f.setAutoStartup(autoStartup);
        return f;
    }
}

