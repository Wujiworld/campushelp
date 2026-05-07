package com.campushelp.life.feed.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.life.feed.config.FeedProperties;
import com.campushelp.life.social.entity.ChUserFollow;
import com.campushelp.life.social.mapper.ChUserFollowMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FeedProfileService {
    private final ChUserFollowMapper followMapper;
    private final StringRedisTemplate redisTemplate;
    private final FeedProperties feedProperties;

    public FeedProfileService(ChUserFollowMapper followMapper,
                              StringRedisTemplate redisTemplate,
                              FeedProperties feedProperties) {
        this.followMapper = followMapper;
        this.redisTemplate = redisTemplate;
        this.feedProperties = feedProperties;
    }

    public boolean isBigV(long authorId) {
        String key = profileKey(authorId);
        String bigV = (String) redisTemplate.opsForHash().get(key, "bigV");
        if (bigV != null) {
            return "1".equals(bigV);
        }
        refreshAuthorProfile(authorId);
        bigV = (String) redisTemplate.opsForHash().get(key, "bigV");
        return "1".equals(bigV);
    }

    public void refreshAuthorProfile(long authorId) {
        long followers = followMapper.selectCount(new QueryWrapper<ChUserFollow>().eq("followee_user_id", authorId));
        Map<String, String> payload = new HashMap<>();
        payload.put("followers", String.valueOf(followers));
        payload.put("bigV", followers >= feedProperties.getPushFollowerThreshold() ? "1" : "0");
        redisTemplate.opsForHash().putAll(profileKey(authorId), payload);
    }

    private String profileKey(long authorId) {
        return feedProperties.getAuthorProfilePrefix() + authorId;
    }
}
