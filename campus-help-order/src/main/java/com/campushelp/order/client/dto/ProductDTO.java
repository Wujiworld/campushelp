package com.campushelp.order.client.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private Long storeId;
    private Integer status;
}
