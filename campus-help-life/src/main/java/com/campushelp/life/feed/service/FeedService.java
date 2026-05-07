package com.campushelp.life.feed.service;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.DomainEventPublisher;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.feed.config.FeedProperties;
import com.campushelp.life.feed.dto.FeedItemDTO;
import com.campushelp.life.feed.dto.FeedPublishRequest;
import com.campushelp.life.feed.dto.FeedTimelineResponse;
import com.campushelp.life.social.service.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FeedService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FeedProperties feedProperties;
    private final FollowService followService;
    private final FeedProfileService feedProfileService;
    private final DomainEventPublisher domainEventPublisher;
    private final MeterRegistry meterRegistry;

    public FeedService(StringRedisTemplate redisTemplate,
                       ObjectMapper objectMapper,
                       FeedProperties feedProperties,
                       FollowService followService,
                       FeedProfileService feedProfileService,
                       @Autowired(required = false) DomainEventPublisher domainEventPublisher,
                       MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.feedProperties = feedProperties;
        this.followService = followService;
        this.feedProfileService = feedProfileService;
        this.domainEventPublisher = domainEventPublisher;
        this.meterRegistry = meterRegistry;
    }

    public FeedItemDTO publish(long authorId, FeedPublishRequest req) {
        String bizType = req.getBizType().trim().toUpperCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        FeedItemDTO item = new FeedItemDTO();
        item.setFeedId(UUID.randomUUID().toString());
        item.setAuthorId(authorId);
        item.setBizType(bizType);
        item.setBizId(req.getBizId().trim());
        item.setContent(req.getContent().trim());
        item.setCreatedAtMs(now);
        String payload = encode(item);
        redisTemplate.opsForZSet().add(authorPostsKey(authorId), payload, now);
        trimZset(authorPostsKey(authorId), feedProperties.getTimelineMaxSize());
        publishFeedEvent(item);
        meterRegistry.counter("campus.feed.publish").increment();
        return item;
    }

    public FeedTimelineResponse timeline(long userId, Long cursorScore, String cursorId, int size) {
        int finalSize = Math.min(Math.max(size, 1), feedProperties.getPageSizeMax());
        long maxScore = cursorScore == null ? Long.MAX_VALUE : cursorScore;
        List<FeedItemDTO> merged = new ArrayList<>();

        for (int shard = 0; shard < feedProperties.getHotShardCount(); shard++) {
            merged.addAll(decodeRange(redisTemplate.opsForZSet().reverseRangeByScore(timelineKey(userId) + ":" + shard, 0, maxScore, 0, finalSize)));
        }
        List<Long> followings = followService.listFollowings(userId);
        for (Long followee : followings) {
            if (followee == null || !feedProfileService.isBigV(followee)) {
                continue;
            }
            merged.addAll(decodeRange(redisTemplate.opsForZSet().reverseRangeByScore(authorPostsKey(followee), 0, maxScore, 0, finalSize)));
        }
        merged.addAll(decodeRange(redisTemplate.opsForZSet().reverseRangeByScore(authorPostsKey(userId), 0, maxScore, 0, finalSize)));

        Map<String, FeedItemDTO> dedup = new LinkedHashMap<>();
        for (FeedItemDTO item : merged) {
            if (item == null || item.getFeedId() == null) {
                continue;
            }
            if (cursorScore != null && Objects.equals(item.getCreatedAtMs(), cursorScore) && cursorId != null && cursorId.equals(item.getFeedId())) {
                continue;
            }
            dedup.putIfAbsent(item.getFeedId(), item);
        }
        List<FeedItemDTO> ordered = new ArrayList<>(dedup.values());
        ordered.sort(Comparator.comparing(FeedItemDTO::getCreatedAtMs, Comparator.nullsLast(Long::compareTo)).reversed()
                .thenComparing(FeedItemDTO::getFeedId, Comparator.nullsLast(String::compareTo)));
        List<FeedItemDTO> pageItems = ordered.size() > finalSize ? ordered.subList(0, finalSize) : ordered;
        FeedItemDTO tail = pageItems.isEmpty() ? null : pageItems.get(pageItems.size() - 1);
        meterRegistry.counter("campus.feed.timeline.read").increment();
        return new FeedTimelineResponse(pageItems, tail == null ? null : tail.getCreatedAtMs(), tail == null ? null : tail.getFeedId());
    }

    public void fanoutToFollowers(FeedItemDTO item) {
        if (item == null || item.getAuthorId() == null) {
            return;
        }
        if (feedProfileService.isBigV(item.getAuthorId())) {
            return;
        }
        String payload = encode(item);
        List<Long> followers = followService.listFollowers(item.getAuthorId());
        for (Long follower : followers) {
            if (follower == null || Objects.equals(follower, item.getAuthorId())) {
                continue;
            }
            String key = timelineShardKey(follower, item.getAuthorId());
            redisTemplate.opsForZSet().add(key, payload, item.getCreatedAtMs());
            trimZset(key, feedProperties.getTimelineMaxSize());
        }
        meterRegistry.counter("campus.feed.fanout").increment(followers.size());
    }

    public FeedItemDTO fromEventPayload(Map<String, Object> payload) {
        if (payload == null) {
            throw new ValidationException("feed payload missing");
        }
        return objectMapper.convertValue(payload, FeedItemDTO.class);
    }

    private void publishFeedEvent(FeedItemDTO item) {
        if (domainEventPublisher == null) {
            return;
        }
        Map<String, Object> payload = objectMapper.convertValue(item, Map.class);
        DomainEvent event = new DomainEvent(
                UUID.randomUUID().toString(),
                NotificationEventType.FEED_POST_PUBLISHED,
                item.getFeedId(),
                Instant.now(),
                new Long[0],
                payload
        );
        domainEventPublisher.publishAfterCommit(event);
    }

    private List<FeedItemDTO> decodeRange(Set<String> encodedSet) {
        List<FeedItemDTO> items = new ArrayList<>();
        if (encodedSet == null) {
            return items;
        }
        for (String encoded : encodedSet) {
            try {
                items.add(objectMapper.readValue(encoded, FeedItemDTO.class));
            } catch (Exception ignored) {
            }
        }
        return items;
    }

    private String encode(FeedItemDTO item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception e) {
            throw new IllegalStateException("feed encode failed", e);
        }
    }

    private void trimZset(String key, int maxSize) {
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > maxSize) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - maxSize - 1);
        }
    }

    private String timelineKey(long userId) {
        return feedProperties.getTimelinePrefix() + userId;
    }

    private String timelineShardKey(long userId, long authorId) {
        int shard = Math.floorMod(Long.hashCode(authorId), feedProperties.getHotShardCount());
        return timelineKey(userId) + ":" + shard;
    }

    private String authorPostsKey(long authorId) {
        return feedProperties.getAuthorPostsPrefix() + authorId;
    }
}
