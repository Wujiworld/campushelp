package com.campushelp.order.controller;

import com.campushelp.order.dto.PaymentMockCallbackRequest;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.exception.BadRequestException;
import com.campushelp.order.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 模拟支付回调（联调 / 演示）；生产应对接真实渠道并由网关鉴权。
 */
@Validated
@RestController
public class PaymentMockController {

    private final OrderService orderService;

    @Value("${campus.payment.mock-secret:}")
    private String mockSecret;

    public PaymentMockController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/v3/payments/mock-callback")
    public ChOrder mockCallback(
            @Valid @RequestBody PaymentMockCallbackRequest req,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Mock-Payment-Secret", required = false) String secret) {
        if (mockSecret != null && !mockSecret.isBlank()) {
            if (secret == null || !mockSecret.equals(secret.trim())) {
                throw new BadRequestException("模拟支付密钥无效");
            }
        }
        if (!"SUCCESS".equalsIgnoreCase(req.getStatus().trim())) {
            throw new BadRequestException("仅支持 status=SUCCESS");
        }
        return orderService.confirmPaidFromMock(req.getOrderId(), req.getPayNo());
    }
}
