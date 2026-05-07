package com.campushelp.life.social.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.secondhand.service.SecondhandCatalogService;
import com.campushelp.life.client.dto.AgentItemDto;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.social.dto.LikeStateResponse;
import com.campushelp.life.social.dto.LikeToggleRequest;
import com.campushelp.life.social.service.LikeService;
import com.campushelp.life.agent.service.AgentCatalogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Validated
@RestController
public class LikeController {

    private final LikeService likeService;
    private final SecondhandCatalogService secondhandCatalogService;
    private final LifeCatalogClient lifeCatalogClient;
    private final AgentCatalogService agentCatalogService;

    public LikeController(LikeService likeService,
                          SecondhandCatalogService secondhandCatalogService,
                          LifeCatalogClient lifeCatalogClient,
                          AgentCatalogService agentCatalogService) {
        this.likeService = likeService;
        this.secondhandCatalogService = secondhandCatalogService;
        this.lifeCatalogClient = lifeCatalogClient;
        this.agentCatalogService = agentCatalogService;
    }

    @GetMapping("/api/v3/likes/state")
    public LikeStateResponse state(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return likeService.getState(SecurityContextUtils.optionalUserId(), targetType, targetId);
    }

    @PostMapping("/api/v3/likes/toggle")
    public LikeStateResponse toggle(@Valid @RequestBody LikeToggleRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        return likeService.toggle(uid, req.getTargetType(), req.getTargetId());
    }

    @GetMapping("/api/v3/likes/mine/secondhand")
    public List<Map<String, Object>> mineSecondhandLikes() {
        long uid = SecurityContextUtils.requireUserId();
        return likeService.listLikedSecondhandIds(uid).stream()
                .map(secondhandCatalogService::getById)
                .filter(Objects::nonNull)
                .map(secondhandCatalogService::toBriefMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/likes/mine/activities")
    public List<ActivityDto> mineActivityLikes() {
        long uid = SecurityContextUtils.requireUserId();
        List<Long> ids = likeService.listLikedActivityIds(uid);
        if (ids.isEmpty()) return new ArrayList<>();
        return lifeCatalogClient.batchActivities(ids);
    }

    @GetMapping("/api/v3/likes/mine/agent")
    public List<AgentItemDto> mineAgentLikes() {
        long uid = SecurityContextUtils.requireUserId();
        List<Long> ids = likeService.listLikedAgentIds(uid);
        if (ids.isEmpty()) return new ArrayList<>();
        // 简化：批量取详情（目前 AgentCatalogService 无批量方法，直接 mapper 由 service 内部使用）
        return ids.stream()
                .map(agentCatalogService::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
