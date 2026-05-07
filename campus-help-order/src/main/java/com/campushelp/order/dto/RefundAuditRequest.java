package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RefundAuditRequest {
    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
