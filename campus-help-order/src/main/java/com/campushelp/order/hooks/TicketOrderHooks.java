package com.campushelp.order.hooks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.order.entity.ChActivityEnroll;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChOrderTicketExt;
import com.campushelp.order.mapper.ChActivityEnrollMapper;
import com.campushelp.order.mapper.ChOrderTicketExtMapper;
import com.campushelp.order.mapper.ChTicketTypeMapper;
import com.campushelp.order.service.OrderService;
import com.campushelp.order.spi.OrderPaidSideEffect;
import com.campushelp.order.spi.OrderUnpaidClosedSideEffect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Order(25)
public class TicketOrderHooks implements OrderPaidSideEffect, OrderUnpaidClosedSideEffect {
    private final ChOrderTicketExtMapper ticketExtMapper;
    private final ChActivityEnrollMapper enrollMapper;
    private final ChTicketTypeMapper ticketTypeMapper;

    public TicketOrderHooks(ChOrderTicketExtMapper ticketExtMapper,
                            ChActivityEnrollMapper enrollMapper,
                            ChTicketTypeMapper ticketTypeMapper) {
        this.ticketExtMapper = ticketExtMapper;
        this.enrollMapper = enrollMapper;
        this.ticketTypeMapper = ticketTypeMapper;
    }

    @Override
    public void onOrderPaid(ChOrder order) {
        if (!OrderService.ORDER_TYPE_TICKET.equals(order.getOrderType())) return;
        ChOrderTicketExt ext = ticketExtMapper.selectOne(new QueryWrapper<ChOrderTicketExt>().eq("order_id", order.getId()));
        if (ext == null) return;
        if (enrollMapper.selectCount(new QueryWrapper<ChActivityEnroll>().eq("order_id", order.getId())) > 0) return;
        String code = "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        ext.setEntryCode(code);
        ticketExtMapper.updateById(ext);
        ChActivityEnroll en = new ChActivityEnroll();
        en.setActivityId(ext.getActivityId());
        en.setTicketTypeId(ext.getTicketTypeId());
        en.setUserId(order.getUserId());
        en.setOrderId(order.getId());
        en.setStatus("SUCCESS");
        en.setEntryCode(code);
        en.setCreatedAt(LocalDateTime.now());
        enrollMapper.insert(en);
    }

    @Override
    public void onOrderUnpaidClosed(ChOrder order) {
        if (!OrderService.ORDER_TYPE_TICKET.equals(order.getOrderType())) return;
        ChOrderTicketExt ext = ticketExtMapper.selectOne(new QueryWrapper<ChOrderTicketExt>().eq("order_id", order.getId()));
        if (ext == null || ext.getTicketTypeId() == null) return;
        ticketTypeMapper.releaseStock(ext.getTicketTypeId(), 1);
    }
}
