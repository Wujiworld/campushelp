package com.campushelp.life.notify.mq.support;

import com.campushelp.common.event.DomainEvent;
import com.campushelp.common.event.NotificationEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationMessageComposer {

    private final ObjectMapper objectMapper;

    public NotificationMessageComposer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Composed compose(DomainEvent ev) {
        if (ev.getType() == NotificationEventType.SYSTEM_ANNOUNCEMENT) {
            String title = str(ev.getPayload(), "title", "系统公告");
            String content = str(ev.getPayload(), "content", "");
            return new Composed(title, content, toJson(ev.getPayload()));
        }

        if (ev.getType().name().startsWith("ORDER_")) {
            String orderNo = str(ev.getPayload(), "orderNo", "");
            String status = str(ev.getPayload(), "status", ev.getType().name());
            String title = "订单状态更新";
            String content = (orderNo.isEmpty() ? "" : ("订单 " + orderNo + " ")) + "状态：" + status;
            return new Composed(title, content, toJson(ev.getPayload()));
        }

        if (ev.getType().name().startsWith("COMMENT_")) {
            String title = "评论通知";
            String content = "评论事件：" + ev.getType().name();
            return new Composed(title, content, toJson(ev.getPayload()));
        }

        if (ev.getType().name().startsWith("ACTIVITY_")) {
            String title = "活动通知";
            String content = "活动事件：" + ev.getType().name();
            return new Composed(title, content, toJson(ev.getPayload()));
        }

        return new Composed("系统通知", ev.getType().name(), toJson(ev.getPayload()));
    }

    private String toJson(Map<String, Object> payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return null;
        }
    }

    private static String str(Map<String, Object> payload, String key, String def) {
        if (payload == null) return def;
        Object v = payload.get(key);
        if (v == null) return def;
        String s = String.valueOf(v);
        return s == null ? def : s;
    }

    @Data
    @AllArgsConstructor
    public static class Composed {
        private String title;
        private String content;
        private String payloadJson;
    }
}

