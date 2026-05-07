package com.campushelp.life.social.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.social.service.FollowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/api/v3/follows/toggle")
    public Map<String, Object> toggle(@RequestParam Long followeeUserId) {
        long uid = SecurityContextUtils.requireUserId();
        boolean following = followService.toggle(uid, followeeUserId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("followeeUserId", followeeUserId);
        resp.put("following", following);
        return resp;
    }

    @GetMapping("/api/v3/follows/mine")
    public List<Long> mine() {
        return followService.listFollowings(SecurityContextUtils.requireUserId());
    }

    @GetMapping("/api/v3/follows/fans")
    public List<Long> fans() {
        return followService.listFollowers(SecurityContextUtils.requireUserId());
    }
}
