package com.campushelp.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.order.dto.OrderCreateRequest;
import com.campushelp.order.dto.OrderLifecyclePhase;
import com.campushelp.order.dto.OrderSummaryView;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.mapper.ChOrderItemMapper;
import com.campushelp.order.mapper.ChOrderMapper;
import com.campushelp.order.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
public class OrderController {

    private final OrderService orderService;
    private final ChOrderItemMapper orderItemMapper;
    private final ChOrderMapper orderMapper;

    public OrderController(OrderService orderService, ChOrderItemMapper orderItemMapper, ChOrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderItemMapper = orderItemMapper;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/api/v3/orders/mine")
    public List<ChOrder> mine(@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return orderService.listMine(SecurityContextUtils.requireUserId(), limit);
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/api/v3/admin/orders")
    public List<ChOrder> adminList(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        int safe = Math.min(Math.max(limit, 1), 200);
        return orderMapper.selectList(
                new QueryWrapper<ChOrder>().orderByDesc("created_at").last("LIMIT " + safe));
    }

    /**
     * 商家：待接单/待出餐（已支付、待确认）。
     */
    @RequireRole(RoleEnum.MERCHANT)
    @GetMapping("/api/v3/orders/merchant/pending")
    public List<ChOrder> merchantPending(@RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderService.listMerchantPending(uid, limit);
    }

    /**
     * 骑手：可抢单列表。
     */
    @RequireRole(RoleEnum.RIDER)
    @GetMapping("/api/v3/orders/rider/pool")
    public List<ChOrder> riderPool(@RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        return orderService.listRiderPool(limit);
    }

    @PostMapping("/api/v3/orders")
    public ChOrder create(@Valid @RequestBody OrderCreateRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        req.setUserId(uid);
        return orderService.create(req);
    }

    @GetMapping("/api/v3/orders/{id}")
    public ChOrder getById(@PathVariable("id") Long id) {
        return orderService.getByIdForUser(id, SecurityContextUtils.requireUserId());
    }

    /**
     * DTO 试点：新接口仅返回对外必要字段，不暴露实体。
     */
    @GetMapping("/api/v3/orders/{id}/summary")
    public OrderSummaryView summary(@PathVariable("id") Long id) {
        ChOrder order = orderService.getByIdForUser(id, SecurityContextUtils.requireUserId());
        return toSummary(order);
    }

    /**
     * 支付（当前为同步确认收款，上线可对接微信/支付宝回调）。
     */
    @PostMapping("/api/v3/orders/{id}/pay")
    public ChOrder pay(@PathVariable("id") Long id) {
        return orderService.pay(id, SecurityContextUtils.requireUserId());
    }

    @PostMapping("/api/v3/orders/{id}/cancel")
    public ChOrder cancel(@PathVariable("id") Long id) {
        return orderService.cancel(id, SecurityContextUtils.requireUserId());
    }

    /**
     * 商家确认接单（需 MERCHANT 角色且为门店归属商家）。
     */
    @RequireRole(RoleEnum.MERCHANT)
    @PostMapping("/api/v3/orders/{id}/merchant/confirm")
    public ChOrder merchantConfirm(@PathVariable("id") Long id) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderService.merchantConfirm(id, uid, uid);
    }

    @RequireRole(RoleEnum.RIDER)
    @PostMapping("/api/v3/orders/{id}/rider/take")
    public ChOrder riderTake(@PathVariable("id") Long id) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderService.riderTake(id, uid, uid);
    }

    @RequireRole(RoleEnum.RIDER)
    @PostMapping("/api/v3/orders/{id}/rider/pickup")
    public ChOrder riderPickup(@PathVariable("id") Long id) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderService.riderPickup(id, uid, uid);
    }

    @PostMapping("/api/v3/orders/{id}/complete")
    public ChOrder complete(@PathVariable("id") Long id) {
        return orderService.complete(id, SecurityContextUtils.requireUserId());
    }

    @GetMapping("/api/v3/orders/{id}/items")
    public List<ChOrderItem> items(@PathVariable("id") Long id) {
        orderService.getByIdForUser(id, SecurityContextUtils.requireUserId());
        return orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChOrderItem>()
                        .eq("order_id", id)
        );
    }

    private static OrderSummaryView toSummary(ChOrder order) {
        OrderSummaryView view = new OrderSummaryView();
        view.setId(order.getId());
        view.setOrderNo(order.getOrderNo());
        view.setOrderType(order.getOrderType());
        view.setStatus(order.getStatus());
        view.setLifecyclePhase(OrderLifecyclePhase.fromOrder(order).name());
        view.setPayStatus(order.getPayStatus());
        view.setTotalAmountCent(order.getTotalAmountCent());
        view.setPayAmountCent(order.getPayAmountCent());
        view.setDeliveryFeeCent(order.getDeliveryFeeCent());
        view.setExpireAt(order.getExpireAt());
        view.setPaidAt(order.getPaidAt());
        view.setCancelledAt(order.getCancelledAt());
        view.setCompletedAt(order.getCompletedAt());
        view.setCreatedAt(order.getCreatedAt());
        view.setUpdatedAt(order.getUpdatedAt());
        return view;
    }
}
