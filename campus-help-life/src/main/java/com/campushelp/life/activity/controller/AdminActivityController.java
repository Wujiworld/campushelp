package com.campushelp.life.activity.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.activity.dto.AdminActivityCreateRequest;
import com.campushelp.life.activity.dto.AdminTicketTypeCreateRequest;
import com.campushelp.life.activity.service.ActivityAdminService;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.dto.TicketTypeDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
public class AdminActivityController {

    private final ActivityAdminService adminService;

    public AdminActivityController(ActivityAdminService adminService) {
        this.adminService = adminService;
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/activities")
    public ActivityDto create(@Valid @RequestBody AdminActivityCreateRequest req) {
        long uid = SecurityContextUtils.requireUserId();
        return adminService.createActivity(uid, req);
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/activities/{id}/tickets")
    public TicketTypeDto createTicket(@PathVariable("id") Long activityId,
                                     @Valid @RequestBody AdminTicketTypeCreateRequest req) {
        return adminService.createTicketType(activityId, req);
    }
}

