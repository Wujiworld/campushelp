package com.campushelp.order.client.dto;

import lombok.Data;

@Data
public class ProductSkuDTO {
    private Long id;
    private Long productId;
    private Integer priceCent;
    private Integer status;
}
