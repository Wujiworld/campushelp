package com.campushelp.life.admin.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.life.agent.service.AgentCatalogService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminAgentController {

    private final AgentCatalogService agentCatalogService;

    public AdminAgentController(AgentCatalogService agentCatalogService) {
        this.agentCatalogService = agentCatalogService;
    }

    @RequireRole(RoleEnum.ADMIN)
    @PostMapping("/api/v3/admin/agent/items/{id}/offline")
    public void offline(@PathVariable Long id) {
        agentCatalogService.adminOffline(id);
    }
}
