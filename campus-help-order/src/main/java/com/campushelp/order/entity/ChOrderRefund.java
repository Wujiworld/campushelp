package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_order_refund")
public class ChOrderRefund {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long merchantUserId;
    private String applyReason;
    private Integer applyAmountCent;
    private String status;
    private Long auditBy;
    private String auditRole;
    private String auditRemark;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
