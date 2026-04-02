package com.campushelp.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_product")
public class ChProduct {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long storeId;
    private String name;
    private String coverUrl;
    private String category;
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

