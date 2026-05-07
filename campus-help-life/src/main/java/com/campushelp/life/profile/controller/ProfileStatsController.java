package com.campushelp.life.profile.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.profile.dto.ProfileStatsVO;
import com.campushelp.life.profile.service.ProfileStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileStatsController {

    private final ProfileStatsService profileStatsService;

    public ProfileStatsController(ProfileStatsService profileStatsService) {
        this.profileStatsService = profileStatsService;
    }

    @GetMapping("/api/v3/profile/stats")
    public ProfileStatsVO stats() {
        return profileStatsService.stats(SecurityContextUtils.requireUserId());
    }
}
