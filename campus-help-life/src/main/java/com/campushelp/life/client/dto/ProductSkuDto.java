package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductSkuDto {
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
