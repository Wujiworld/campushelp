package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_order_ticket_ext")
public class ChOrderTicketExt {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    private Long activityId;
    private Long ticketTypeId;
    private String entryCode;
    private LocalDateTime createdAt;
}
