package com.campushelp.life.merchant.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
public class MerchantSkuUpdateRequest {

    @Size(max = 64)
    private String skuName;

    @Min(0)
    private Integer priceCent;

    @Min(0)
    private Integer stock;
}
