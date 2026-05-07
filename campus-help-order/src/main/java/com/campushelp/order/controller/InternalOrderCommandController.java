package com.campushelp.order.controller;

import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class InternalOrderCommandController {

    private final OrderService orderService;

    public InternalOrderCommandController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/v3/internal/orders/agent/purchase")
    public ChOrder createAgentPurchase(@RequestParam("userId") long userId,
                                       @RequestParam("campusId") long campusId,
                                       @RequestParam("agentItemId") long agentItemId,
                                       @RequestParam("addressId") long addressId,
                                       @RequestParam(value = "remark", required = false) String remark) {
        return orderService.createAgentPurchase(userId, campusId, agentItemId, addressId, remark);
    }

    @PostMapping("/api/v3/internal/orders/errand")
    public ChOrder createErrandOrder(@RequestParam("userId") long userId,
                                     @RequestParam("campusId") long campusId,
                                     @RequestParam(value = "addressId", required = false) Long addressId,
                                     @RequestParam(value = "errandType", required = false) String errandType,
                                     @RequestParam(value = "pickupAddress", required = false) String pickupAddress,
                                     @RequestParam(value = "pickupCode", required = false) String pickupCode,
                                     @RequestParam(value = "listText", required = false) String listText,
                                     @RequestParam("feeCent") int feeCent,
                                     @RequestParam(value = "remark", required = false) String remark) {
        return orderService.createErrandOrder(
                userId, campusId, addressId, errandType, pickupAddress, pickupCode, listText, feeCent, remark);
    }

    @PostMapping("/api/v3/internal/orders/secondhand/purchase")
    public ChOrder createSecondhandPurchase(@RequestParam("userId") long userId,
                                            @RequestParam("campusId") long campusId,
                                            @RequestParam("itemId") long itemId,
                                            @RequestParam(value = "deliveryMode", required = false) String deliveryMode,
                                            @RequestParam(value = "addressId", required = false) Long addressId) {
        return orderService.createSecondhandPurchase(userId, campusId, itemId, deliveryMode, addressId);
    }

    @PostMapping("/api/v3/internal/orders/ticket")
    public ChOrder createTicketOrder(@RequestParam("userId") long userId,
                                     @RequestParam("campusId") long campusId,
                                     @RequestParam("ticketTypeId") long ticketTypeId) {
        return orderService.createTicketOrder(userId, campusId, ticketTypeId);
    }

    @PostMapping("/api/v3/internal/orders/{orderId}/close-unpaid")
    public boolean tryCloseUnpaidOrder(@PathVariable("orderId") long orderId) {
        return orderService.tryCloseUnpaidOrder(orderId);
    }
}
