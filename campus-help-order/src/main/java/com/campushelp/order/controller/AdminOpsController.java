package com.campushelp.order.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.order.entity.ChSystemConfig;
import com.campushelp.order.service.AdminOpsService;
import com.campushelp.order.service.AuditLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequireRole(RoleEnum.ADMIN)
public class AdminOpsController {

    private final AdminOpsService adminOpsService;
    private final AuditLogService auditLogService;

    public AdminOpsController(AdminOpsService adminOpsService, AuditLogService auditLogService) {
        this.adminOpsService = adminOpsService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/api/v3/admin/system/configs")
    public List<ChSystemConfig> listConfigs() {
        return adminOpsService.listConfigs();
    }

    @PostMapping("/api/v3/admin/system/configs")
    public ChSystemConfig upsert(@RequestParam @NotBlank String key,
                                 @RequestParam @NotBlank String value) {
        Long uid = SecurityContextUtils.requireUserId();
        ChSystemConfig c = adminOpsService.upsertConfig(key, value, uid);
        auditLogService.log(uid, "ADMIN", "SYSTEM_CONFIG_UPSERT", "SYSTEM_CONFIG", key, "value=" + value);
        return c;
    }

    @GetMapping("/api/v3/admin/system/dashboard")
    public Map<String, Object> dashboard() {
        return adminOpsService.dashboard();
    }

    @GetMapping("/api/v3/admin/system/audit-logs")
    public List<com.campushelp.order.entity.ChAuditLog> auditLogs(@RequestParam(defaultValue = "100") int limit) {
        return auditLogService.list(limit);
    }
}
