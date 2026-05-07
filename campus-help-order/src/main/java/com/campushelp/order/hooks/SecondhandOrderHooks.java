package com.campushelp.order.hooks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderSecondhandExt;
import com.campushelp.order.entity.ChSecondhandItem;
import com.campushelp.order.mapper.ChOrderSecondhandExtMapper;
import com.campushelp.order.mapper.ChSecondhandItemMapper;
import com.campushelp.order.service.OrderService;
import com.campushelp.order.spi.OrderPaidSideEffect;
import com.campushelp.order.spi.OrderUnpaidClosedSideEffect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(10)
public class SecondhandOrderHooks implements OrderPaidSideEffect, OrderUnpaidClosedSideEffect {
    private final ChOrderSecondhandExtMapper secondhandExtMapper;
    private final ChSecondhandItemMapper secondhandItemMapper;

    public SecondhandOrderHooks(ChOrderSecondhandExtMapper secondhandExtMapper, ChSecondhandItemMapper secondhandItemMapper) {
        this.secondhandExtMapper = secondhandExtMapper;
        this.secondhandItemMapper = secondhandItemMapper;
    }

    @Override
    public void onOrderPaid(ChOrder order) {
        if (!OrderService.ORDER_TYPE_SECONDHAND.equals(order.getOrderType())) return;
        ChOrderSecondhandExt ext = secondhandExtMapper.selectOne(new QueryWrapper<ChOrderSecondhandExt>().eq("order_id", order.getId()));
        if (ext == null) return;
        secondhandItemMapper.update(null, new UpdateWrapper<ChSecondhandItem>()
                .eq("id", ext.getSecondhandItemId())
                .eq("status", OrderService.SECONDHAND_ITEM_PENDING_PAY)
                .set("status", OrderService.SECONDHAND_ITEM_SOLD)
                .set("updated_at", LocalDateTime.now()));
    }

    @Override
    public void onOrderUnpaidClosed(ChOrder order) {
        if (!OrderService.ORDER_TYPE_SECONDHAND.equals(order.getOrderType())) return;
        ChOrderSecondhandExt ext = secondhandExtMapper.selectOne(new QueryWrapper<ChOrderSecondhandExt>().eq("order_id", order.getId()));
        if (ext == null) return;
        secondhandItemMapper.update(null, new UpdateWrapper<ChSecondhandItem>()
                .eq("id", ext.getSecondhandItemId())
                .eq("status", OrderService.SECONDHAND_ITEM_PENDING_PAY)
                .set("status", OrderService.SECONDHAND_ITEM_ON_SALE)
                .set("updated_at", LocalDateTime.now()));
    }
}
