package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_ticket_type")
public class ChTicketType {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;
    private String name;
    private Integer priceCent;
    private Integer stockTotal;
    private Integer stockSold;
    private Integer perUserLimit;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
