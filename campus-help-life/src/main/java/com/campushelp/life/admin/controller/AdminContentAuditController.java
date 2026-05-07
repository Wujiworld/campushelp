package com.campushelp.life.admin.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.life.admin.service.AdminContentAuditService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Map;

@Validated
@RestController
@RequireRole(RoleEnum.ADMIN)
public class AdminContentAuditController {

    private final AdminContentAuditService adminContentAuditService;

    public AdminContentAuditController(AdminContentAuditService adminContentAuditService) {
        this.adminContentAuditService = adminContentAuditService;
    }

    @GetMapping("/api/v3/admin/content/pending")
    public Map<String, Object> pending(@RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return adminContentAuditService.pending(limit);
    }

    @PostMapping("/api/v3/admin/content/{contentType}/{id}/approve")
    public void approve(@PathVariable String contentType, @PathVariable Long id) {
        adminContentAuditService.approve(contentType, id);
    }

    @PostMapping("/api/v3/admin/content/{contentType}/{id}/reject")
    public void reject(@PathVariable String contentType, @PathVariable Long id) {
        adminContentAuditService.reject(contentType, id);
    }
}
