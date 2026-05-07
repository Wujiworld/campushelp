package com.campushelp.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 业务域事件：用于异步通知/站内信/WebSocket 推送等。
 * <p>
 * 约束：
 * - eventId 全局唯一（用于幂等）
 * - payload 尽量包含展示所需字段，避免前端收到推送后再做 N 次查询
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {
    /** 全局唯一：幂等键 */
    private String eventId;
    /** 事件类型 */
    private NotificationEventType type;
    /** 业务主键（如 orderId / commentId / activityId） */
    private String bizId;
    /** 事件发生时间（毫秒精度） */
    private Instant occurredAt;
    /** 目标收件人（用户 ID 列表）；为空表示仅广播类事件（由 consumer 决定） */
    private Long[] recipients;
    /** 事件载荷（JSON） */
    private Map<String, Object> payload;
}

