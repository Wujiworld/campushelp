package com.campushelp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.dto.OrderItemRequest;
import com.campushelp.order.dto.OrderCreateRequest;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.exception.BadRequestException;
import com.campushelp.order.exception.OrderNotFoundException;
import com.campushelp.order.mapper.ChOrderMapper;
import com.campushelp.order.mapper.ChOrderItemMapper;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String PAY_UNPAID = "UNPAID";
    public static final String PAY_PAID = "PAID";

    private final ChOrderMapper orderMapper;
    private final ChOrderItemMapper orderItemMapper;

    @Value("${campus.order.pay-timeout-minutes:15}")
    private int payTimeoutMinutes;

    public OrderService(ChOrderMapper orderMapper, ChOrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder create(OrderCreateRequest req) {
        LocalDateTime now = LocalDateTime.now();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("订单至少需要 1 个明细项");
        }

        int itemsAmount = 0;
        for (OrderItemRequest it : req.getItems()) {
            itemsAmount += (it.getUnitPriceCent() * it.getQuantity());
        }

        int totalAmountCent = itemsAmount + req.getDeliveryFeeCent();

        ChOrder o = new ChOrder();
        o.setOrderNo(OrderNoGenerator.next());
        o.setOrderType(req.getOrderType().trim().toUpperCase());
        o.setUserId(req.getUserId());
        o.setStoreId(req.getStoreId());
        o.setCampusId(req.getCampusId());
        o.setAddressId(req.getAddressId());
        o.setStatus(STATUS_CREATED);
        o.setPayStatus(PAY_UNPAID);
        o.setTotalAmountCent(totalAmountCent);
        o.setPayAmountCent(0);
        o.setDeliveryFeeCent(req.getDeliveryFeeCent());
        o.setRemark(req.getRemark());
        o.setExpireAt(now.plusMinutes(payTimeoutMinutes));
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        orderMapper.insert(o);

        // 明细落库（snapshot_json 只做历史留痕）
        for (OrderItemRequest it : req.getItems()) {
            ChOrderItem oi = new ChOrderItem();
            oi.setOrderId(o.getId());
            oi.setItemType("SKU");
            oi.setRefId(it.getSkuId());
            oi.setTitle(it.getTitle());
            oi.setUnitPriceCent(it.getUnitPriceCent());
            oi.setQuantity(it.getQuantity());
            oi.setAmountCent(it.getUnitPriceCent() * it.getQuantity());
            oi.setSnapshotJson(JSONUtil.toJsonStr(it));
            oi.setCreatedAt(now);
            orderItemMapper.insert(oi);
        }

        return o;
    }

    public ChOrder getByIdOrThrow(Long id) {
        ChOrder o = orderMapper.selectById(id);
        if (o == null) {
            throw new OrderNotFoundException("订单不存在: " + id);
        }
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder pay(Long orderId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!o.getUserId().equals(operatorUserId)) {
            throw new BadRequestException("无权支付该订单");
        }
        if (!STATUS_CREATED.equals(o.getStatus()) || !PAY_UNPAID.equals(o.getPayStatus())) {
            throw new BadRequestException("当前状态不可支付");
        }
        LocalDateTime now = LocalDateTime.now();
        if (o.getExpireAt() != null && now.isAfter(o.getExpireAt())) {
            throw new BadRequestException("订单已超时，请重新下单");
        }
        o.setPayStatus(PAY_PAID);
        o.setStatus(STATUS_PAID);
        o.setPayAmountCent(o.getTotalAmountCent());
        o.setPaidAt(now);
        o.setUpdatedAt(now);
        orderMapper.updateById(o);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder cancel(Long orderId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!o.getUserId().equals(operatorUserId)) {
            throw new BadRequestException("无权取消该订单");
        }
        if (!STATUS_CREATED.equals(o.getStatus()) || !PAY_UNPAID.equals(o.getPayStatus())) {
            throw new BadRequestException("当前状态不可取消");
        }
        LocalDateTime now = LocalDateTime.now();
        o.setStatus(STATUS_CANCELLED);
        o.setCancelledAt(now);
        o.setUpdatedAt(now);
        orderMapper.updateById(o);
        return o;
    }

    public static final String STATUS_MERCHANT_CONFIRMED = "MERCHANT_CONFIRMED";
    public static final String STATUS_RIDER_TAKEN = "RIDER_TAKEN";
    public static final String STATUS_DELIVERING = "DELIVERING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @Transactional(rollbackFor = Exception.class)
    public ChOrder merchantConfirm(Long orderId, Long merchantUserId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!STATUS_PAID.equals(o.getStatus()) || !PAY_PAID.equals(o.getPayStatus())) {
            throw new BadRequestException("当前状态不可商家确认");
        }
        // V1 演示：merchant_user_id 允许幂等写入
        if (o.getMerchantUserId() == null) {
            o.setMerchantUserId(merchantUserId);
        } else if (!o.getMerchantUserId().equals(merchantUserId)) {
            throw new BadRequestException("非本商家订单");
        }
        o.setStatus(STATUS_MERCHANT_CONFIRMED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder riderTake(Long orderId, Long riderUserId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!STATUS_MERCHANT_CONFIRMED.equals(o.getStatus())) {
            throw new BadRequestException("当前状态不可骑手接单");
        }
        if (o.getRiderUserId() != null && !o.getRiderUserId().equals(riderUserId)) {
            throw new BadRequestException("该订单已被其他骑手接单");
        }
        o.setRiderUserId(riderUserId);
        o.setStatus(STATUS_RIDER_TAKEN);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder riderPickup(Long orderId, Long riderUserId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!STATUS_RIDER_TAKEN.equals(o.getStatus())) {
            throw new BadRequestException("当前状态不可开始配送");
        }
        if (o.getRiderUserId() == null || !o.getRiderUserId().equals(riderUserId)) {
            throw new BadRequestException("非本骑手订单");
        }
        o.setStatus(STATUS_DELIVERING);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public ChOrder complete(Long orderId, Long operatorUserId) {
        ChOrder o = getByIdOrThrow(orderId);
        if (!STATUS_DELIVERING.equals(o.getStatus())) {
            throw new BadRequestException("当前状态不可完成");
        }
        if (!o.getUserId().equals(operatorUserId)) {
            throw new BadRequestException("无权完成该订单");
        }
        LocalDateTime now = LocalDateTime.now();
        o.setStatus(STATUS_COMPLETED);
        o.setCompletedAt(now);
        o.setUpdatedAt(now);
        orderMapper.updateById(o);
        return o;
    }

    /**
     * 超时未支付自动关单（首版用定时任务扫描；生产可换 RabbitMQ 延迟队列削峰）。
     */
    @Transactional(rollbackFor = Exception.class)
    public int closeExpiredUnpaid() {
        LocalDateTime now = LocalDateTime.now();
        // 避免 LambdaQueryWrapper 在 Java 23 下的反射兼容问题，改用字段名字符串。
        QueryWrapper<ChOrder> q = new QueryWrapper<>();
        q.eq("status", STATUS_CREATED)
                .eq("pay_status", PAY_UNPAID)
                .isNotNull("expire_at")
                .le("expire_at", now);
        List<ChOrder> list = orderMapper.selectList(q);
        int n = 0;
        for (ChOrder o : list) {
            UpdateWrapper<ChOrder> u = new UpdateWrapper<>();
            u.eq("id", o.getId())
                    .eq("status", STATUS_CREATED)
                    .eq("pay_status", PAY_UNPAID);
            ChOrder update = new ChOrder();
            update.setStatus(STATUS_CANCELLED);
            update.setCancelledAt(now);
            update.setUpdatedAt(now);
            if (orderMapper.update(update, u) > 0) {
                n++;
            }
        }
        return n;
    }
}
