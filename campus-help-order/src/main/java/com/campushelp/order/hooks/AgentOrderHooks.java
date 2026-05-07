package com.campushelp.order.hooks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.entity.ChAgentItem;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.mapper.ChAgentItemMapper;
import com.campushelp.order.mapper.ChOrderItemMapper;
import com.campushelp.order.service.OrderService;
import com.campushelp.order.spi.OrderUnpaidClosedSideEffect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(15)
public class AgentOrderHooks implements OrderUnpaidClosedSideEffect {
    private final ChOrderItemMapper orderItemMapper;
    private final ChAgentItemMapper agentItemMapper;

    public AgentOrderHooks(ChOrderItemMapper orderItemMapper, ChAgentItemMapper agentItemMapper) {
        this.orderItemMapper = orderItemMapper;
        this.agentItemMapper = agentItemMapper;
    }

    @Override
    public void onOrderUnpaidClosed(ChOrder order) {
        if (!OrderService.ORDER_TYPE_ERRAND.equals(order.getOrderType())) return;
        ChOrderItem line = orderItemMapper.selectOne(new QueryWrapper<ChOrderItem>()
                .eq("order_id", order.getId()).eq("item_type", "AGENT").last("LIMIT 1"));
        if (line == null || line.getRefId() == null) return;
        agentItemMapper.update(null, new UpdateWrapper<ChAgentItem>()
                .eq("id", line.getRefId()).eq("status", "OFFLINE")
                .set("status", "ON_SALE").set("updated_at", LocalDateTime.now()));
    }
}
