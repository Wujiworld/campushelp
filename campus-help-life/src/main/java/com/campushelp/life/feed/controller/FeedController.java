package com.campushelp.life.feed.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.feed.dto.FeedItemDTO;
import com.campushelp.life.feed.dto.FeedPublishRequest;
import com.campushelp.life.feed.dto.FeedTimelineResponse;
import com.campushelp.life.feed.service.FeedService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {
    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @PostMapping("/api/v3/feed/publish")
    public FeedItemDTO publish(@Validated @RequestBody FeedPublishRequest request) {
        long uid = SecurityContextUtils.requireUserId();
        return feedService.publish(uid, request);
    }

    @GetMapping("/api/v3/feed/timeline")
    public FeedTimelineResponse timeline(@RequestParam(value = "cursorScore", required = false) Long cursorScore,
                                         @RequestParam(value = "cursorId", required = false) String cursorId,
                                         @RequestParam(value = "size", defaultValue = "20") int size) {
        long uid = SecurityContextUtils.requireUserId();
        return feedService.timeline(uid, cursorScore, cursorId, size);
    }
}
