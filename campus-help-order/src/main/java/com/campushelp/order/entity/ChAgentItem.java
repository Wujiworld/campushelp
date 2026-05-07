package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_agent_item")
public class ChAgentItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sellerUserId;
    private Long campusId;
    private String title;
    private String description;
    private Integer priceCent;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
