package com.campushelp.life.mq;

import com.campushelp.life.client.OrderServiceClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "campus.mq.order-delay.enabled", havingValue = "true")
@ConditionalOnBean(RabbitTemplate.class)
public class OrderCloseUnpaidListener {

    private final OrderServiceClient orderServiceClient;

    public OrderCloseUnpaidListener(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @RabbitListener(queues = OrderDelayRabbitConfig.QUEUE_CLOSE)
    public void onClose(Long orderId) {
        if (orderId == null) {
            return;
        }
        orderServiceClient.tryCloseUnpaidOrder(orderId);
    }
}
