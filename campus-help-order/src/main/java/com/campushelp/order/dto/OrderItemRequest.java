package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderItemRequest {

    private Long skuId;

    @NotBlank
    private String title;

    @NotNull
    @Min(0)
    private Integer unitPriceCent;

    @NotNull
    @Min(1)
    private Integer quantity;
}

