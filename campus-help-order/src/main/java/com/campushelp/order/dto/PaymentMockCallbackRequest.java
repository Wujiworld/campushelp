package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PaymentMockCallbackRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String payNo;

    /** 仅支持 SUCCESS */
    @NotBlank
    private String status;
}
