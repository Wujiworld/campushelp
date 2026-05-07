package com.campushelp.life.social.service;

import com.campushelp.common.api.ResultCode;
import com.campushelp.common.exception.BusinessException;
import com.campushelp.life.client.OrderSocialClient;
import com.campushelp.life.social.dto.LikeStateResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {

    public static final String TARGET_SECONDHAND = "SECONDHAND_ITEM";
    public static final String TARGET_ACTIVITY = "ACTIVITY";
    public static final String TARGET_AGENT = "AGENT_ITEM";
    public static final String TARGET_COMMENT = "COMMENT";

    private final OrderSocialClient orderSocialClient;

    public LikeService(OrderSocialClient orderSocialClient) {
        this.orderSocialClient = orderSocialClient;
    }

    public LikeStateResponse getState(long userIdOrZero, String targetType, long targetId) {
        String tt = normalizeType(targetType);
        ensureTargetExists(tt, targetId);
        return orderSocialClient.likeState(userIdOrZero, tt, targetId);
    }

    public LikeStateResponse toggle(long userId, String targetType, long targetId) {
        String tt = normalizeType(targetType);
        ensureTargetExists(tt, targetId);
        return orderSocialClient.toggleLike(userId, tt, targetId);
    }

    public List<Long> listLikedSecondhandIds(long userId) {
        return orderSocialClient.listLikedIds(userId, TARGET_SECONDHAND);
    }

    public List<Long> listLikedActivityIds(long userId) {
        return orderSocialClient.listLikedIds(userId, TARGET_ACTIVITY);
    }

    public List<Long> listLikedAgentIds(long userId) {
        return orderSocialClient.listLikedIds(userId, TARGET_AGENT);
    }

    public List<Long> listLikedCommentIds(long userId) {
        return orderSocialClient.listLikedIds(userId, TARGET_COMMENT);
    }

    private String normalizeType(String raw) {
        if (raw == null) {
            throw new BusinessException(ResultCode.BIZ_RULE, "targetType 不能为空");
        }
        String t = raw.trim().toUpperCase();
        if (TARGET_SECONDHAND.equals(t) || "SECONDHAND".equals(t)) {
            return TARGET_SECONDHAND;
        }
        if (TARGET_ACTIVITY.equals(t)) {
            return TARGET_ACTIVITY;
        }
        if (TARGET_AGENT.equals(t) || "AGENT".equals(t)) {
            return TARGET_AGENT;
        }
        if (TARGET_COMMENT.equals(t) || "COMMENTS".equals(t) || "COMMENT_ITEM".equals(t)) {
            return TARGET_COMMENT;
        }
        throw new BusinessException(ResultCode.BIZ_RULE, "不支持的 targetType");
    }

    private void ensureTargetExists(String targetType, long targetId) {
        Boolean ok = orderSocialClient.targetExists(targetType, targetId);
        if (!Boolean.TRUE.equals(ok)) {
            throw new BusinessException(ResultCode.BIZ_RULE, "目标不存在");
        }
    }
}
