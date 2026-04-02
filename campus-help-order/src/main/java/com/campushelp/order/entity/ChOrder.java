package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_order")
public class ChOrder {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;
    private String orderType;
    private Long userId;
    private Long storeId;
    private Long merchantUserId;
    private Long riderUserId;
    private Long campusId;
    private Long addressId;

    private String status;
    private String payStatus;

    private Integer totalAmountCent;
    private Integer payAmountCent;
    private Integer deliveryFeeCent;

    private String remark;
    private LocalDateTime expireAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

