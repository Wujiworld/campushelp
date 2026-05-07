package com.campushelp.user.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.user.dto.RoleApplicationAuditRequest;
import com.campushelp.user.entity.ChRoleApplication;
import com.campushelp.user.entity.ChUser;
import com.campushelp.user.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequireRole(RoleEnum.ADMIN)
public class AdminUserController {

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/api/v3/admin/role-applications")
    public List<ChRoleApplication> listRoleApplications(@RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return authService.listRoleApplications(status, limit);
    }

    @GetMapping("/api/v3/admin/users")
    public List<ChUser> users(@RequestParam(required = false) Integer status,
                              @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return authService.listUsers(status, limit);
    }

    @PostMapping("/api/v3/admin/users/{id}/freeze")
    public ChUser freeze(@PathVariable("id") Long userId) {
        return authService.updateUserStatus(userId, 0);
    }

    @PostMapping("/api/v3/admin/users/{id}/unfreeze")
    public ChUser unfreeze(@PathVariable("id") Long userId) {
        return authService.updateUserStatus(userId, 1);
    }

    @PostMapping("/api/v3/admin/role-applications/{id}/approve")
    public ChRoleApplication approve(@PathVariable("id") Long id,
                                     @Valid @RequestBody RoleApplicationAuditRequest req) {
        return authService.approveRoleApplication(id, SecurityContextUtils.requireUserId(), req.getRemark());
    }

    @PostMapping("/api/v3/admin/role-applications/{id}/reject")
    public ChRoleApplication reject(@PathVariable("id") Long id,
                                    @Valid @RequestBody RoleApplicationAuditRequest req) {
        return authService.rejectRoleApplication(id, SecurityContextUtils.requireUserId(), req.getRemark());
    }
}
