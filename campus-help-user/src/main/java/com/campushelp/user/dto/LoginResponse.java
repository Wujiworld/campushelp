package com.campushelp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回：前端在后续请求头携带 {@code Authorization: Bearer &lt;token&gt;}。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private long expiresInMs;
    private String[] roles;
}
