package com.campushelp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户注册：默认赋予「学生」角色（种子数据 role_id=1001）。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6~64")
    private String password;

    /** 可选展示名 */
    private String nickname;

    /**
     * 注册时希望获得的角色：{@code STUDENT}（默认）、{@code RIDER}、{@code MERCHANT}。
     * 不可自助注册 {@code ADMIN}。
     */
    private String requestedRole;

    /** 当服务端配置了商家注册邀请码时，注册 {@code MERCHANT} 必填且一致 */
    private String inviteCode;

    /** 资质材料类型（如 STUDENT_CARD / BUSINESS_LICENSE / HEALTH_CERT） */
    private String qualificationDocType;

    /** 资质编号（如学号/执照号），可选 */
    private String qualificationDocNo;

    /** 资质图片 URL 列表（最多 5 张） */
    private List<String> qualificationImageUrls;

    /** 申请说明 */
    private String qualificationRemark;
}
