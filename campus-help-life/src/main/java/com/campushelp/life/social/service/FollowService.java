package com.campushelp.life.social.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.DomainEventPublisher;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.life.feed.service.FeedProfileService;
import com.campushelp.life.social.entity.ChUserFollow;
import com.campushelp.life.social.mapper.ChUserFollowMapper;
import com.campushelp.common.exception.BusinessException;
import com.campushelp.common.api.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final ChUserFollowMapper followMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final FeedProfileService feedProfileService;

    public FollowService(ChUserFollowMapper followMapper,
                         @Autowired(required = false) DomainEventPublisher domainEventPublisher,
                         @Autowired(required = false) FeedProfileService feedProfileService) {
        this.followMapper = followMapper;
        this.domainEventPublisher = domainEventPublisher;
        this.feedProfileService = feedProfileService;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(long followerUserId, long followeeUserId) {
        if (followerUserId == followeeUserId) {
            throw new BusinessException(ResultCode.BIZ_RULE, "不能关注自己");
        }
        ChUserFollow exists = followMapper.selectOne(new QueryWrapper<ChUserFollow>()
                .eq("follower_user_id", followerUserId)
                .eq("followee_user_id", followeeUserId));
        if (exists != null) {
            followMapper.deleteById(exists.getId());
            refreshFeedProfile(followeeUserId);
            return false;
        }
        ChUserFollow f = new ChUserFollow();
        f.setFollowerUserId(followerUserId);
        f.setFolloweeUserId(followeeUserId);
        f.setCreatedAt(LocalDateTime.now());
        followMapper.insert(f);
        refreshFeedProfile(followeeUserId);
        publishFollowEvent(followerUserId, followeeUserId);
        return true;
    }

    public List<Long> listFollowings(long userId) {
        return followMapper.selectList(new QueryWrapper<ChUserFollow>()
                        .eq("follower_user_id", userId)
                        .orderByDesc("created_at"))
                .stream().map(ChUserFollow::getFolloweeUserId).collect(Collectors.toList());
    }

    public List<Long> listFollowers(long userId) {
        return followMapper.selectList(new QueryWrapper<ChUserFollow>()
                        .eq("followee_user_id", userId)
                        .orderByDesc("created_at"))
                .stream().map(ChUserFollow::getFollowerUserId).collect(Collectors.toList());
    }

    private void publishFollowEvent(long followerUserId, long followeeUserId) {
        if (domainEventPublisher == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("followerUserId", followerUserId);
        payload.put("followeeUserId", followeeUserId);
        DomainEvent event = new DomainEvent(
                UUID.randomUUID().toString(),
                NotificationEventType.FOLLOW_CREATED,
                String.valueOf(followeeUserId),
                Instant.now(),
                new Long[]{followeeUserId},
                payload
        );
        domainEventPublisher.publishAfterCommit(event);
    }

    private void refreshFeedProfile(long followeeUserId) {
        if (feedProfileService != null) {
            feedProfileService.refreshAuthorProfile(followeeUserId);
        }
    }
}
