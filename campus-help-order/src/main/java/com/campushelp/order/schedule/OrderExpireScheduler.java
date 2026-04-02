package com.campushelp.order.schedule;

import com.campushelp.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付超时关单：固定间隔扫描，避免强依赖 MQ 即可本地跑通主链路。
 * 替代方案：RabbitMQ 延迟插件 / TTL+死信队列 / 时间轮。
 */
@Component
public class OrderExpireScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpireScheduler.class);

    private final OrderService orderService;

    public OrderExpireScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${campus.order.expire-scan-ms:30000}")
    public void closeExpired() {
        int n = orderService.closeExpiredUnpaid();
        if (n > 0) {
            log.info("超时自动取消未支付订单数量: {}", n);
        }
    }
}
