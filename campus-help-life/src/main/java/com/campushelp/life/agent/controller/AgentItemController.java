package com.campushelp.life.agent.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.agent.dto.AgentItemCreateRequest;
import com.campushelp.life.agent.service.AgentCatalogService;
import com.campushelp.life.client.dto.AgentItemDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
public class AgentItemController {

    private final AgentCatalogService agentCatalogService;

    public AgentItemController(AgentCatalogService agentCatalogService) {
        this.agentCatalogService = agentCatalogService;
    }

    @GetMapping("/api/v3/agent/items")
    public List<AgentItemDto> list(
            @RequestParam(required = false) Long campusId,
            @RequestParam(defaultValue = "ON_SALE") String status) {
        return agentCatalogService.list(campusId, status);
    }

    @GetMapping("/api/v3/agent/items/{id}")
    public AgentItemDto detail(@PathVariable Long id) {
        AgentItemDto it = agentCatalogService.getById(id);
        if (it == null) {
            throw new ValidationException("条目不存在");
        }
        return it;
    }

    @PostMapping("/api/v3/agent/items")
    public AgentItemDto publish(@Valid @RequestBody AgentItemCreateRequest req, @RequestParam Long campusId) {
        long uid = SecurityContextUtils.requireUserId();
        if (SecurityContextUtils.hasRole("RIDER") && !SecurityContextUtils.hasRole("ADMIN")) {
            throw new ValidationException("骑手账号不可发布代购");
        }
        if (!SecurityContextUtils.hasRole("STUDENT") && !SecurityContextUtils.hasRole("MERCHANT")
                && !SecurityContextUtils.hasRole("ADMIN")) {
            throw new ValidationException("仅学生或商家可发布代购");
        }
        return agentCatalogService.publish(uid, campusId, req);
    }

    @PostMapping("/api/v3/agent/items/{id}/offline")
    public void offline(@PathVariable Long id) {
        agentCatalogService.offline(SecurityContextUtils.requireUserId(), id);
    }
}
