package com.campushelp.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.entity.ChActivity;
import com.campushelp.order.entity.ChAgentItem;
import com.campushelp.order.entity.ChComment;
import com.campushelp.order.entity.ChLike;
import com.campushelp.order.entity.ChMessage;
import com.campushelp.order.entity.ChMessageRecipient;
import com.campushelp.order.entity.ChOrder;
import com.campushelp.order.entity.ChSecondhandItem;
import com.campushelp.order.mapper.ChActivityMapper;
import com.campushelp.order.mapper.ChAgentItemMapper;
import com.campushelp.order.mapper.ChCommentMapper;
import com.campushelp.order.mapper.ChLikeMapper;
import com.campushelp.order.mapper.ChMessageMapper;
import com.campushelp.order.mapper.ChMessageRecipientMapper;
import com.campushelp.order.mapper.ChOrderMapper;
import com.campushelp.order.mapper.ChSecondhandItemMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class InternalLifeSocialNotifyProfileController {
    private final ChLikeMapper likeMapper;
    private final ChSecondhandItemMapper secondhandItemMapper;
    private final ChActivityMapper activityMapper;
    private final ChAgentItemMapper agentItemMapper;
    private final ChCommentMapper commentMapper;
    private final ChMessageMapper messageMapper;
    private final ChMessageRecipientMapper recipientMapper;
    private final ChOrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    public InternalLifeSocialNotifyProfileController(ChLikeMapper likeMapper,
                                                     ChSecondhandItemMapper secondhandItemMapper,
                                                     ChActivityMapper activityMapper,
                                                     ChAgentItemMapper agentItemMapper,
                                                     ChCommentMapper commentMapper,
                                                     ChMessageMapper messageMapper,
                                                     ChMessageRecipientMapper recipientMapper,
                                                     ChOrderMapper orderMapper,
                                                     ObjectMapper objectMapper) {
        this.likeMapper = likeMapper;
        this.secondhandItemMapper = secondhandItemMapper;
        this.activityMapper = activityMapper;
        this.agentItemMapper = agentItemMapper;
        this.commentMapper = commentMapper;
        this.messageMapper = messageMapper;
        this.recipientMapper = recipientMapper;
        this.orderMapper = orderMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/v3/internal/life/likes/state")
    public Map<String, Object> likeState(@RequestParam("userId") long userId,
                                         @RequestParam("targetType") String targetType,
                                         @RequestParam("targetId") long targetId) {
        int count = currentLikeCount(targetType, targetId);
        boolean liked = userId > 0 && likeMapper.selectCount(new QueryWrapper<ChLike>()
                .eq("user_id", userId).eq("target_type", targetType).eq("target_id", targetId)) > 0;
        Map<String, Object> out = new HashMap<>();
        out.put("liked", liked);
        out.put("count", count);
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/v3/internal/life/likes/toggle")
    public Map<String, Object> toggleLike(@RequestParam("userId") long userId,
                                          @RequestParam("targetType") String targetType,
                                          @RequestParam("targetId") long targetId) {
        QueryWrapper<ChLike> q = new QueryWrapper<ChLike>().eq("user_id", userId)
                .eq("target_type", targetType).eq("target_id", targetId);
        ChLike existing = likeMapper.selectOne(q);
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            adjustCount(targetType, targetId, -1);
        } else {
            ChLike l = new ChLike();
            l.setUserId(userId);
            l.setTargetType(targetType);
            l.setTargetId(targetId);
            l.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(l);
            adjustCount(targetType, targetId, 1);
        }
        return likeState(userId, targetType, targetId);
    }

    @GetMapping("/api/v3/internal/life/likes/ids")
    public List<Long> listLikedIds(@RequestParam("userId") long userId,
                                   @RequestParam("targetType") String targetType) {
        return likeMapper.selectList(new QueryWrapper<ChLike>()
                        .eq("user_id", userId).eq("target_type", targetType).orderByDesc("created_at").last("LIMIT 200"))
                .stream().map(ChLike::getTargetId).collect(Collectors.toList());
    }

    @GetMapping("/api/v3/internal/life/messages/inbox")
    public List<Map<String, Object>> listInbox(@RequestParam("userId") long userId,
                                               @RequestParam("page") int page,
                                               @RequestParam("size") int size) {
        int sz = Math.min(Math.max(1, size), 100);
        int off = Math.max(0, page) * sz;
        List<ChMessageRecipient> inbox = recipientMapper.selectList(new QueryWrapper<ChMessageRecipient>()
                .eq("user_id", userId).isNull("deleted_at").orderByDesc("created_at").last("LIMIT " + off + "," + sz));
        List<Long> msgIds = inbox.stream().map(ChMessageRecipient::getMessageId).collect(Collectors.toList());
        Map<Long, ChMessageRecipient> recByMsgId = new HashMap<>();
        for (ChMessageRecipient r : inbox) recByMsgId.put(r.getMessageId(), r);
        List<ChMessage> announcements = messageMapper.selectList(new QueryWrapper<ChMessage>()
                .eq("type", "SYSTEM_ANNOUNCEMENT").orderByDesc("created_at").last("LIMIT " + sz));
        for (ChMessage m : announcements) if (!msgIds.contains(m.getId())) msgIds.add(m.getId());
        if (msgIds.isEmpty()) return new ArrayList<>();
        List<ChMessage> messages = messageMapper.selectBatchIds(msgIds);
        Map<Long, ChMessage> msgById = new LinkedHashMap<>();
        for (ChMessage m : messages) msgById.put(m.getId(), m);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Long id : msgIds) {
            ChMessage m = msgById.get(id);
            if (m == null) continue;
            ChMessageRecipient r = recByMsgId.get(id);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("type", m.getType());
            item.put("title", m.getTitle());
            item.put("content", m.getContent());
            item.put("payload", parsePayload(m.getPayloadJson()));
            item.put("createdAt", m.getCreatedAt());
            item.put("readAt", r == null ? null : r.getReadAt());
            out.add(item);
        }
        return out;
    }

    @PostMapping("/api/v3/internal/life/messages/read")
    public void markRead(@RequestParam("userId") long userId, @RequestParam("messageId") long messageId) {
        ChMessageRecipient exist = recipientMapper.selectOne(new QueryWrapper<ChMessageRecipient>()
                .eq("user_id", userId).eq("message_id", messageId).last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (exist == null) {
            ChMessageRecipient r = new ChMessageRecipient();
            r.setMessageId(messageId);
            r.setUserId(userId);
            r.setReadAt(now);
            r.setCreatedAt(now);
            recipientMapper.insert(r);
            return;
        }
        if (exist.getReadAt() == null) {
            recipientMapper.update(null, new UpdateWrapper<ChMessageRecipient>().eq("id", exist.getId()).set("read_at", now));
        }
    }

    @PostMapping("/api/v3/internal/life/messages/upsert")
    public void upsertEventMessage(@RequestParam("eventId") String eventId,
                                   @RequestParam("type") String type,
                                   @RequestParam(value = "bizId", required = false) String bizId,
                                   @RequestParam("title") String title,
                                   @RequestParam("content") String content,
                                   @RequestParam(value = "payloadJson", required = false) String payloadJson,
                                   @RequestParam(value = "recipients", required = false) List<Long> recipients) {
        ChMessage existing = messageMapper.selectOne(new QueryWrapper<ChMessage>().eq("event_id", eventId).last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        ChMessage m = new ChMessage();
        m.setEventId(eventId);
        m.setType(type);
        m.setBizId(bizId);
        m.setTitle(title);
        m.setContent(content);
        m.setPayloadJson(payloadJson);
        m.setCreatedAt(now);
        messageMapper.insert(m);
        if (!"SYSTEM_ANNOUNCEMENT".equals(type) && recipients != null) {
            for (Long uid : recipients) {
                if (uid == null) continue;
                ChMessageRecipient r = new ChMessageRecipient();
                r.setMessageId(m.getId());
                r.setUserId(uid);
                r.setCreatedAt(now);
                try { recipientMapper.insert(r); } catch (Exception ignored) {}
            }
        }
    }

    @GetMapping("/api/v3/internal/life/profile/stats")
    public Map<String, Object> stats(@RequestParam("userId") long userId) {
        Map<String, Object> v = new HashMap<>();
        v.put("secondhandOnSale", (int) secondhandItemMapper.selectCount(new QueryWrapper<ChSecondhandItem>()
                .eq("seller_user_id", userId).eq("status", "ON_SALE")));
        v.put("secondhandSold", (int) secondhandItemMapper.selectCount(new QueryWrapper<ChSecondhandItem>()
                .eq("seller_user_id", userId).eq("status", "SOLD")));
        v.put("buyerOrderCount", (int) orderMapper.selectCount(new QueryWrapper<ChOrder>().eq("user_id", userId)));
        v.put("likesGiven", (int) likeMapper.selectCount(new QueryWrapper<ChLike>().eq("user_id", userId)));
        return v;
    }

    @GetMapping("/api/v3/internal/life/likes/targets/{targetType}/{targetId}")
    public Boolean targetExists(@PathVariable("targetType") String targetType, @PathVariable("targetId") long targetId) {
        if ("SECONDHAND_ITEM".equals(targetType)) return secondhandItemMapper.selectById(targetId) != null;
        if ("ACTIVITY".equals(targetType)) return activityMapper.selectById(targetId) != null;
        if ("AGENT_ITEM".equals(targetType)) return agentItemMapper.selectById(targetId) != null;
        if ("COMMENT".equals(targetType)) return commentMapper.selectById(targetId) != null;
        return false;
    }

    private int currentLikeCount(String targetType, long targetId) {
        if ("SECONDHAND_ITEM".equals(targetType)) {
            ChSecondhandItem it = secondhandItemMapper.selectById(targetId);
            return it != null && it.getLikeCount() != null ? it.getLikeCount() : 0;
        }
        if ("ACTIVITY".equals(targetType)) {
            ChActivity a = activityMapper.selectById(targetId);
            return a != null && a.getLikeCount() != null ? a.getLikeCount() : 0;
        }
        return (int) likeMapper.selectCount(new QueryWrapper<ChLike>().eq("target_type", targetType).eq("target_id", targetId));
    }

    private void adjustCount(String targetType, long targetId, int delta) {
        if ("SECONDHAND_ITEM".equals(targetType)) {
            secondhandItemMapper.update(null, new UpdateWrapper<ChSecondhandItem>()
                    .eq("id", targetId)
                    .setSql(delta > 0 ? "like_count = IFNULL(like_count, 0) + 1"
                            : "like_count = IF(IFNULL(like_count, 0) > 0, like_count - 1, 0)"));
            return;
        }
        if ("ACTIVITY".equals(targetType)) {
            activityMapper.update(null, new UpdateWrapper<ChActivity>()
                    .eq("id", targetId)
                    .setSql(delta > 0 ? "like_count = IFNULL(like_count, 0) + 1"
                            : "like_count = IF(IFNULL(like_count, 0) > 0, like_count - 1, 0)"));
        }
    }

    private Map<String, Object> parsePayload(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
