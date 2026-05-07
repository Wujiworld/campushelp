package com.campushelp.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campushelp.order.entity.ChOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChOrderMapper extends BaseMapper<ChOrder> {

    @Select("""
            SELECT o.* FROM ch_order o
            WHERE o.rider_user_id IS NULL
              AND (
                (o.order_type = 'TAKEOUT' AND o.status = 'MERCHANT_CONFIRMED')
                OR (o.order_type = 'ERRAND' AND o.status = 'PAID')
                OR (
                  o.order_type = 'SECONDHAND' AND o.status = 'PAID'
                  AND EXISTS (
                    SELECT 1 FROM ch_order_secondhand_ext e
                    WHERE e.order_id = o.id AND e.delivery_mode = 'DELIVERY'
                  )
                )
              )
            ORDER BY o.created_at ASC
            LIMIT #{limit}
            """)
    List<ChOrder> selectRiderPool(@Param("limit") int limit);

    @Select("""
            SELECT COUNT(*) FROM ch_order o
            WHERE o.pay_status = 'PAID'
              AND o.paid_at IS NOT NULL
              AND o.paid_at > DATE_SUB(NOW(3), INTERVAL 48 HOUR)
              AND NOT EXISTS (
                SELECT 1 FROM ch_payment_notify n
                WHERE n.order_id = o.id AND n.status = 'SUCCESS'
              )
            """)
    long countPaidOrdersMissingSuccessNotify();

    @Select("""
            SELECT COUNT(*) FROM ch_payment_notify n
            INNER JOIN ch_order o ON o.id = n.order_id
            WHERE n.status = 'SUCCESS'
              AND o.pay_status <> 'PAID'
              AND n.created_at > DATE_SUB(NOW(3), INTERVAL 48 HOUR)
            """)
    long countSuccessNotifyButOrderNotPaid();

    @Select("""
            SELECT COALESCE(SUM(pay_amount_cent), 0)
            FROM ch_order
            WHERE pay_status = 'PAID'
            """)
    long sumPaidAmountCent();

    @Select("""
            SELECT COUNT(DISTINCT user_id)
            FROM ch_order
            WHERE created_at >= DATE_SUB(NOW(3), INTERVAL 7 DAY)
            """)
    long countActiveOrderUsers7d();

    @Select("""
            SELECT COUNT(*)
            FROM ch_order
            WHERE order_type = 'TICKET'
              AND created_at >= DATE_SUB(NOW(3), INTERVAL 24 HOUR)
            """)
    long countTicketOrders24h();
}

