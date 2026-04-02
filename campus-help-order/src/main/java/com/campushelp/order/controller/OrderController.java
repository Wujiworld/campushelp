package com.campushelp.order.controller;

import com.campushelp.order.dto.OrderCreateRequest;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.mapper.ChOrderItemMapper;
import com.campushelp.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final ChOrderItemMapper orderItemMapper;

    public OrderController(OrderService orderService, ChOrderItemMapper orderItemMapper) {
        this.orderService = orderService;
        this.orderItemMapper = orderItemMapper;
    }

    @PostMapping("/api/orders")
    public ChOrder create(@Valid @RequestBody OrderCreateRequest req) {
        return orderService.create(req);
    }

    @GetMapping("/api/orders/{id}")
    public ChOrder getById(@PathVariable("id") Long id) {
        return orderService.getByIdOrThrow(id);
    }

    /**
     * 模拟支付成功（主链路：待支付 → 已支付待后续履约）。
     */
    @PostMapping("/api/orders/{id}/pay")
    public ChOrder pay(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) {
        return orderService.pay(id, userId);
    }

    /**
     * 用户主动取消（仅待支付）。
     */
    @PostMapping("/api/orders/{id}/cancel")
    public ChOrder cancel(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) {
        return orderService.cancel(id, userId);
    }

    /**
     * 商家确认（外卖：商家接单/出餐前置）
     */
    @PostMapping("/api/orders/{id}/merchant/confirm")
    public ChOrder merchantConfirm(
            @PathVariable("id") Long id,
            @RequestParam("merchantUserId") Long merchantUserId,
            @RequestParam(value = "operatorUserId", required = false) Long operatorUserId) {
        // V1：先用 merchantUserId 作为操作者
        Long op = operatorUserId == null ? merchantUserId : operatorUserId;
        return orderService.merchantConfirm(id, merchantUserId, op);
    }

    /**
     * 骑手接单
     */
    @PostMapping("/api/orders/{id}/rider/take")
    public ChOrder riderTake(
            @PathVariable("id") Long id,
            @RequestParam("riderUserId") Long riderUserId) {
        return orderService.riderTake(id, riderUserId, riderUserId);
    }

    /**
     * 骑手开始配送（取餐）
     */
    @PostMapping("/api/orders/{id}/rider/pickup")
    public ChOrder riderPickup(
            @PathVariable("id") Long id,
            @RequestParam("riderUserId") Long riderUserId) {
        return orderService.riderPickup(id, riderUserId, riderUserId);
    }

    /**
     * 用户确认收货（完成）
     */
    @PostMapping("/api/orders/{id}/complete")
    public ChOrder complete(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) {
        return orderService.complete(id, userId);
    }

    /**
     * 订单明细查询
     */
    @GetMapping("/api/orders/{id}/items")
    public List<ChOrderItem> items(@PathVariable("id") Long id) {
        // 简化：先用 MyBatis-Plus selectByMap/条件查询
        return orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChOrderItem>()
                        .eq("order_id", id)
        );
    }
}
