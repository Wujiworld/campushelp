package com.campushelp.order.dto;

import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.service.OrderService;

/**
 * 对外聚合生命周期（与 DB 细粒度 status 并存）：面试/前端可用 FULFILLING 口径。
 */
public enum OrderLifecyclePhase {
    AWAITING_PAYMENT,
    FULFILLING,
    COMPLETED,
    CANCELLED;

    public static OrderLifecyclePhase fromOrder(ChOrder o) {
        if (o == null) {
            return CANCELLED;
        }
        if (OrderService.STATUS_CANCELLED.equals(o.getStatus())) {
            return CANCELLED;
        }
        if (OrderService.STATUS_COMPLETED.equals(o.getStatus())) {
            return COMPLETED;
        }
        if (OrderService.STATUS_CREATED.equals(o.getStatus())
                && OrderService.PAY_UNPAID.equals(o.getPayStatus())) {
            return AWAITING_PAYMENT;
        }
        if (OrderService.PAY_PAID.equals(o.getPayStatus())) {
            return FULFILLING;
        }
        return FULFILLING;
    }
}
