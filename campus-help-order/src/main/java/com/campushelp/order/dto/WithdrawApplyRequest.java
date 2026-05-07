package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Data
public class WithdrawApplyRequest {
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "MERCHANT|RIDER", message = "role 仅支持 MERCHANT 或 RIDER")
    private String role;

    @NotNull(message = "提现金额不能为空")
    @Positive(message = "提现金额必须大于 0")
    private Integer amountCent;

    @NotBlank(message = "收款账号不能为空")
    private String accountNo;

    @NotBlank(message = "收款人不能为空")
    private String accountName;

    private String remark;
}
