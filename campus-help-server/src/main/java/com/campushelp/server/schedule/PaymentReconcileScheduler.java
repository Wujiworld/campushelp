package com.campushelp.server.schedule;

import com.campushelp.order.mapper.ChOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 支付对账骨架：本地订单与回调幂等表一致性探测（可扩展为渠道对账）。
 */
@Component
@ConditionalOnBean(ChOrderMapper.class)
public class PaymentReconcileScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconcileScheduler.class);

    private final ChOrderMapper orderMapper;

    public PaymentReconcileScheduler(ChOrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Scheduled(cron = "${campus.payment.reconcile-cron:0 0 * * * ?}")
    public void reconcile() {
        long missingNotify = orderMapper.countPaidOrdersMissingSuccessNotify();
        long mismatched = orderMapper.countSuccessNotifyButOrderNotPaid();
        if (missingNotify > 0 || mismatched > 0) {
            log.warn("Payment reconcile anomalies (48h window): paidOrdersMissingNotify={}, notifySuccessButOrderNotPaid={}",
                    missingNotify, mismatched);
        }
    }
}
