package com.campushelp.order.spi;

import com.campushelp.order.entity.ChOrder;

/**
 * 未支付订单被取消或超时关闭时回调。
 */
public interface OrderUnpaidClosedSideEffect {

    void onOrderUnpaidClosed(ChOrder order);
}
