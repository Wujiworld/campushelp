package com.campushelp.life.client;

import com.campushelp.life.social.dto.LikeStateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "campus-help-order")
public interface OrderSocialClient {

    @GetMapping("/api/v3/internal/life/likes/state")
    LikeStateResponse likeState(@RequestParam("userId") long userId,
                                @RequestParam("targetType") String targetType,
                                @RequestParam("targetId") long targetId);

    @PostMapping("/api/v3/internal/life/likes/toggle")
    LikeStateResponse toggleLike(@RequestParam("userId") long userId,
                                 @RequestParam("targetType") String targetType,
                                 @RequestParam("targetId") long targetId);

    @GetMapping("/api/v3/internal/life/likes/ids")
    List<Long> listLikedIds(@RequestParam("userId") long userId,
                            @RequestParam("targetType") String targetType);

    @GetMapping("/api/v3/internal/life/likes/targets/{targetType}/{targetId}")
    Boolean targetExists(@PathVariable("targetType") String targetType,
                         @PathVariable("targetId") long targetId);
}
