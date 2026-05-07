package com.campushelp.life.errand.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.client.OrderServiceClient;
import com.campushelp.life.client.dto.OrderSummaryDto;
import com.campushelp.life.errand.dto.ErrandOrderCreateRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
public class ErrandController {

    private final OrderServiceClient orderServiceClient;

    public ErrandController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @PostMapping("/api/v3/errands/orders")
    public OrderSummaryDto create(@Valid @RequestBody ErrandOrderCreateRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderServiceClient.createErrandOrder(
                uid,
                req.getCampusId(),
                req.getAddressId(),
                req.getErrandType(),
                req.getPickupAddress(),
                req.getPickupCode(),
                req.getListText(),
                req.getFeeCent(),
                req.getRemark());
    }
}
