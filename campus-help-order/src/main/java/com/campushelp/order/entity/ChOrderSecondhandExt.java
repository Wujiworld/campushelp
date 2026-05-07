package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_order_secondhand_ext")
public class ChOrderSecondhandExt {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    private Long secondhandItemId;
    private String deliveryMode;
    private LocalDateTime createdAt;
}
