package com.campushelp.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ch_product_sku")
public class ChProductSku {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long productId;
    private String skuName;
    private Integer priceCent;
    private Integer stock;
    private Integer soldCount;
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

