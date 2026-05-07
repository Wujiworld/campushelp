package com.campushelp.user.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.user.dto.UserProfileUpdateRequest;
import com.campushelp.user.dto.UserProfileVO;
import com.campushelp.user.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人资料（昵称、头像、主校区）。
 */
@RestController
public class UserProfileController {

    private final AuthService authService;

    public UserProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/api/v3/users/me")
    public UserProfileVO me() {
        return authService.getProfile(SecurityContextUtils.requireUserId());
    }

    @PatchMapping("/api/v3/users/me")
    public UserProfileVO updateMe(@RequestBody UserProfileUpdateRequest req) {
        return authService.updateProfile(SecurityContextUtils.requireUserId(), req);
    }
}
