package com.campushelp.order.spi;

import com.campushelp.order.entity.ChOrder;

/**
 * 订单创建后的延迟任务（如关单）；默认无实现，由 MQ 模块可选装配。
 */
public interface OrderDelayNotifier {

    /**
     * 任意「待支付」订单创建后触发（外卖/票务/跑腿/二手等）。
     */
    default void onUnpaidOrderCreated(ChOrder order) {
    }

    /**
     * 兼容旧调用：与 {@link #onUnpaidOrderCreated} 等价。
     */
    default void onTakeoutOrderCreated(ChOrder order) {
        onUnpaidOrderCreated(order);
    }
}
