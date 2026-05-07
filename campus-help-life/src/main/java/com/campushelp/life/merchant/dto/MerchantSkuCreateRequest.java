package com.campushelp.life.merchant.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MerchantSkuCreateRequest {

    @NotNull
    private Long productId;

    @Size(max = 64)
    private String skuName;

    @NotNull
    @Min(0)
    private Integer priceCent;

    @NotNull
    @Min(0)
    private Integer stock;
}
