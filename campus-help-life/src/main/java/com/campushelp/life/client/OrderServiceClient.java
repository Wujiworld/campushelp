package com.campushelp.life.client;

import com.campushelp.life.client.dto.OrderSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "campus-help-order")
public interface OrderServiceClient {

    @PostMapping("/api/v3/internal/orders/agent/purchase")
    OrderSummaryDto createAgentPurchase(@RequestParam("userId") long userId,
                                        @RequestParam("campusId") long campusId,
                                        @RequestParam("agentItemId") long agentItemId,
                                        @RequestParam("addressId") long addressId,
                                        @RequestParam(value = "remark", required = false) String remark);

    @PostMapping("/api/v3/internal/orders/errand")
    OrderSummaryDto createErrandOrder(@RequestParam("userId") long userId,
                                      @RequestParam("campusId") long campusId,
                                      @RequestParam(value = "addressId", required = false) Long addressId,
                                      @RequestParam(value = "errandType", required = false) String errandType,
                                      @RequestParam(value = "pickupAddress", required = false) String pickupAddress,
                                      @RequestParam(value = "pickupCode", required = false) String pickupCode,
                                      @RequestParam(value = "listText", required = false) String listText,
                                      @RequestParam("feeCent") int feeCent,
                                      @RequestParam(value = "remark", required = false) String remark);

    @PostMapping("/api/v3/internal/orders/secondhand/purchase")
    OrderSummaryDto createSecondhandPurchase(@RequestParam("userId") long userId,
                                             @RequestParam("campusId") long campusId,
                                             @RequestParam("itemId") long itemId,
                                             @RequestParam(value = "deliveryMode", required = false) String deliveryMode,
                                             @RequestParam(value = "addressId", required = false) Long addressId);

    @PostMapping("/api/v3/internal/orders/ticket")
    OrderSummaryDto createTicketOrder(@RequestParam("userId") long userId,
                                      @RequestParam("campusId") long campusId,
                                      @RequestParam("ticketTypeId") long ticketTypeId);

    @PostMapping("/api/v3/internal/orders/{orderId}/close-unpaid")
    boolean tryCloseUnpaidOrder(@PathVariable("orderId") long orderId);
}
