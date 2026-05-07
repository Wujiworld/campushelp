package com.campushelp.common.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CommonRabbitListenerConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "manualAckRabbitListenerContainerFactory")
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
