package com.campushelp.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_order_errand_ext")
public class ChOrderErrandExt {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;
    private String errandType;
    private String pickupAddress;
    private String pickupCode;
    private String listText;
    private LocalDateTime createdAt;
}
