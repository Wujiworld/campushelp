package com.campushelp.life.agent.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.agent.dto.AgentOrderCreateRequest;
import com.campushelp.life.client.OrderServiceClient;
import com.campushelp.life.client.dto.OrderSummaryDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
public class AgentOrderController {

    private final OrderServiceClient orderServiceClient;

    public AgentOrderController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @PostMapping("/api/v3/agent/orders")
    public OrderSummaryDto create(@Valid @RequestBody AgentOrderCreateRequest req) {
        long uid = SecurityContextUtils.requireUserId();
        return orderServiceClient.createAgentPurchase(
                uid,
                req.getCampusId(),
                req.getAgentItemId(),
                req.getAddressId(),
                req.getRemark());
    }
}

