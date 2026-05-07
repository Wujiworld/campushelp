package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class RefundApplyRequest {
    @NotBlank(message = "退款原因不能为空")
    private String reason;

    @NotNull(message = "退款金额不能为空")
    @Positive(message = "退款金额必须大于 0")
    private Integer amountCent;
}
