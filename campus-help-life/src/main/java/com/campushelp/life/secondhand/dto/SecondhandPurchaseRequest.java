package com.campushelp.life.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SecondhandPurchaseRequest {

    @NotNull
    private Long campusId;

    @NotNull
    private Long itemId;

    @NotBlank
    private String deliveryMode;

    private Long addressId;
}
