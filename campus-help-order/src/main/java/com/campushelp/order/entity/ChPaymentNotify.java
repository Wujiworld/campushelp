package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_payment_notify")
public class ChPaymentNotify {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    private String payNo;
    private String status;

    private LocalDateTime createdAt;
}
