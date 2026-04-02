package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_order_item")
public class ChOrderItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private String itemType;
    private Long refId;

    private String title;
    private Integer unitPriceCent;
    private Integer quantity;
    private Integer amountCent;

    private String snapshotJson;

    private LocalDateTime createdAt;
}

