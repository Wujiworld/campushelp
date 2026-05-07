package com.campushelp.life.seckill.listener;

import com.campushelp.life.seckill.config.SeckillRabbitConfig;
import com.campushelp.life.client.OrderServiceClient;
import com.campushelp.life.seckill.dto.SeckillTicketMessage;
import com.campushelp.life.seckill.service.SeckillTicketService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "campus.seckill.enabled", havingValue = "true")
@ConditionalOnBean(RabbitTemplate.class)
public class SeckillOrderListener {

    private final OrderServiceClient orderServiceClient;
    private final SeckillTicketService seckillTicketService;

    public SeckillOrderListener(OrderServiceClient orderServiceClient, SeckillTicketService seckillTicketService) {
        this.orderServiceClient = orderServiceClient;
        this.seckillTicketService = seckillTicketService;
    }

    @RabbitListener(queues = SeckillRabbitConfig.QUEUE)
    public void onMessage(SeckillTicketMessage msg) {
        try {
            orderServiceClient.createTicketOrder(msg.getUserId(), msg.getCampusId(), msg.getTicketTypeId());
        } catch (RuntimeException e) {
            seckillTicketService.compensateStock(msg.getTicketTypeId());
            throw e;
        }
    }
}
