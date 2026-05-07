package com.campushelp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RoleApplicationAuditRequest {

    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
