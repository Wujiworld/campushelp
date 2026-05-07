package com.campushelp.user.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.user.dto.LoginRequest;
import com.campushelp.user.dto.LoginResponse;
import com.campushelp.user.dto.RegisterRequest;
import com.campushelp.user.dto.UserProfileVO;
import com.campushelp.user.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证接口：与订单服务联调时，先调本服务拿到 Token，再请求 order 的受保护 API。
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/v3/auth/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/api/v3/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /**
     * 当前登录用户信息（需 Authorization: Bearer）。
     */
    @GetMapping("/api/v3/auth/me")
    public UserProfileVO me() {
        return authService.getProfile(SecurityContextUtils.requireUserId());
    }
}
