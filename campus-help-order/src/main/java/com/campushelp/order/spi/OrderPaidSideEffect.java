package com.campushelp.order.spi;

import com.campushelp.order.entity.ChOrder;

/**
 * 支付成功后由订单域回调；实现类可放在 life 等模块。
 */
public interface OrderPaidSideEffect {

    void onOrderPaid(ChOrder order);
}
