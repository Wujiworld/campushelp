package com.campushelp.life.client;

import com.campushelp.life.profile.dto.ProfileStatsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "campus-help-order")
public interface OrderProfileClient {

    @GetMapping("/api/v3/internal/life/profile/stats")
    ProfileStatsVO stats(@RequestParam("userId") long userId);
}
