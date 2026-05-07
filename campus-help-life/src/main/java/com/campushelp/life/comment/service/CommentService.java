package com.campushelp.life.comment.service;

import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.comment.dto.CommentCreateRequest;
import com.campushelp.life.comment.dto.CommentView;
import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.ProductServiceClient;
import com.campushelp.life.client.dto.ActivityDto;
import com.campushelp.life.client.dto.AgentItemDto;
import com.campushelp.life.client.dto.CommentDto;
import com.campushelp.life.client.dto.SecondhandItemDto;
import com.campushelp.life.client.dto.StoreDto;
import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.DomainEventPublisher;
import com.campushelp.common.event.NotificationEventType;
import com.campushelp.common.safety.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {

    public static final String TARGET_ACTIVITY = "ACTIVITY";
    public static final String TARGET_SECONDHAND = "SECONDHAND_ITEM";
    public static final String TARGET_AGENT = "AGENT_ITEM";
    public static final String TARGET_STORE = "STORE";

    private final LifeCatalogClient lifeCatalogClient;
    private final ProductServiceClient productServiceClient;
    private final DomainEventPublisher domainEventPublisher;
    private final SensitiveWordService sensitiveWordService;

    public CommentService(LifeCatalogClient lifeCatalogClient,
                          ProductServiceClient productServiceClient,
                          @Autowired(required = false) DomainEventPublisher domainEventPublisher,
                          SensitiveWordService sensitiveWordService) {
        this.lifeCatalogClient = lifeCatalogClient;
        this.productServiceClient = productServiceClient;
        this.domainEventPublisher = domainEventPublisher;
        this.sensitiveWordService = sensitiveWordService;
    }

    public List<CommentView> listVisible(String targetType, long targetId, int page, int size) {
        String tt = normalizeTargetType(targetType);
        int sz = Math.min(Math.max(1, size), 100);
        int off = Math.max(0, page) * sz;
        List<CommentDto> list = lifeCatalogClient.listComments(tt, targetId, "VISIBLE", off, sz);
        return list.stream()
                .map(c -> new CommentView(c.getId(), c.getUserId(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<CommentDto> listMineRaw(long userId, int page, int size) {
        int sz = Math.min(Math.max(1, size), 100);
        int off = Math.max(0, page) * sz;
        return lifeCatalogClient.listMyComments(userId, off, sz);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentView create(long userId, CommentCreateRequest req) {
        String tt = normalizeTargetType(req.getTargetType());
        validateTarget(tt, req.getTargetId());
        String text = sanitize(req.getContent());
        if (sensitiveWordService != null && sensitiveWordService.containsSensitive(text)) {
            if (sensitiveWordService.mode() == SensitiveWordService.Mode.REJECT) {
                throw new ValidationException("内容包含敏感词");
            }
            text = sensitiveWordService.mask(text);
        }
        if (text.isEmpty()) {
            throw new ValidationException("评论内容不能为空");
        }
        CommentDto c = lifeCatalogClient.createComment(userId, tt, req.getTargetId(), text, "VISIBLE");
        publishCommentEvent(NotificationEventType.COMMENT_CREATED, c, resolveCommentRecipients(tt, req.getTargetId(), userId));
        return new CommentView(c.getId(), c.getUserId(), c.getContent(), c.getCreatedAt());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByAuthorOrAdmin(long actorUserId, long commentId, boolean actorIsAdmin) {
        CommentDto c = lifeCatalogClient.getComment(commentId);
        if (c == null) {
            throw new ValidationException("评论不存在");
        }
        if (!actorIsAdmin && !c.getUserId().equals(actorUserId)) {
            throw new ValidationException("无权删除");
        }
        lifeCatalogClient.deleteComment(commentId);
        publishCommentEvent(NotificationEventType.COMMENT_DELETED, c, new Long[]{c.getUserId()});
    }

    @Transactional(rollbackFor = Exception.class)
    public void hideByAdmin(long commentId) {
        CommentDto c = lifeCatalogClient.getComment(commentId);
        if (c == null) {
            throw new ValidationException("评论不存在");
        }
        lifeCatalogClient.updateCommentStatus(commentId, "HIDDEN");
        publishCommentEvent(NotificationEventType.COMMENT_HIDDEN, c, new Long[]{c.getUserId()});
    }

    private String normalizeTargetType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("targetType 不能为空");
        }
        String t = raw.trim().toUpperCase(Locale.ROOT);
        if (TARGET_ACTIVITY.equals(t) || TARGET_SECONDHAND.equals(t) || TARGET_AGENT.equals(t) || TARGET_STORE.equals(t)) {
            return t;
        }
        throw new ValidationException("不支持的 targetType");
    }

    private void validateTarget(String targetType, long targetId) {
        if (TARGET_ACTIVITY.equals(targetType)) {
            ActivityDto a = lifeCatalogClient.getActivity(targetId);
            if (a == null || !"PUBLISHED".equals(a.getStatus())) {
                throw new ValidationException("活动不可评论");
            }
            return;
        }
        if (TARGET_SECONDHAND.equals(targetType)) {
            SecondhandItemDto it = lifeCatalogClient.getSecondhandItem(targetId);
            if (it == null || !"ON_SALE".equals(it.getStatus())) {
                throw new ValidationException("商品不可评论");
            }
            return;
        }
        if (TARGET_AGENT.equals(targetType)) {
            AgentItemDto it = lifeCatalogClient.getAgentItem(targetId);
            if (it == null || !"ON_SALE".equals(it.getStatus())) {
                throw new ValidationException("代购条目不可评论");
            }
            return;
        }
        if (TARGET_STORE.equals(targetType)) {
            StoreDto s = productServiceClient.getStore(targetId);
            if (s == null || s.getStatus() == null || s.getStatus() != 1) {
                throw new ValidationException("门店不可评论");
            }
        }
    }

    private static String sanitize(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.length() > 512) {
            t = t.substring(0, 512);
        }
        return t.replace('<', '＜').replace('>', '＞');
    }

    private void publishCommentEvent(NotificationEventType type, CommentDto c, Long[] recipients) {
        if (domainEventPublisher == null || c == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("commentId", c.getId());
        payload.put("userId", c.getUserId());
        payload.put("targetType", c.getTargetType());
        payload.put("targetId", c.getTargetId());
        payload.put("status", c.getStatus());
        payload.put("createdAt", c.getCreatedAt());
        DomainEvent ev = new DomainEvent(
                UUID.randomUUID().toString(),
                type,
                String.valueOf(c.getId()),
                Instant.now(),
                recipients == null ? new Long[0] : recipients,
                payload
        );
        domainEventPublisher.publishAfterCommit(ev);
    }

    private Long[] resolveCommentRecipients(String targetType, long targetId, long commentAuthorUserId) {
        // 评论创建：通知目标作者/发布者 + 评论作者本人（便于多端同步）
        java.util.LinkedHashSet<Long> set = new java.util.LinkedHashSet<>();
        set.add(commentAuthorUserId);
        try {
            if (TARGET_ACTIVITY.equals(targetType)) {
                ActivityDto a = lifeCatalogClient.getActivity(targetId);
                if (a != null && a.getCreatedBy() != null) set.add(a.getCreatedBy());
            } else if (TARGET_SECONDHAND.equals(targetType)) {
                SecondhandItemDto it = lifeCatalogClient.getSecondhandItem(targetId);
                if (it != null && it.getSellerUserId() != null) set.add(it.getSellerUserId());
            } else if (TARGET_AGENT.equals(targetType)) {
                AgentItemDto it = lifeCatalogClient.getAgentItem(targetId);
                if (it != null && it.getSellerUserId() != null) set.add(it.getSellerUserId());
            } else if (TARGET_STORE.equals(targetType)) {
                StoreDto s = productServiceClient.getStore(targetId);
                if (s != null && s.getMerchantUserId() != null) set.add(s.getMerchantUserId());
            }
        } catch (Exception ignored) {
        }
        return set.toArray(new Long[0]);
    }
}
