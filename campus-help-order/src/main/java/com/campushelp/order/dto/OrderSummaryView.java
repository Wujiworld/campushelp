package com.campushelp.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单对外摘要视图，作为 DTO 试点避免直接暴露实体。
 */
@Data
public class OrderSummaryView {
    private Long id;
    private String orderNo;
    private String orderType;
    /** DB 原始状态 */
    private String status;
    /** 聚合阶段：AWAITING_PAYMENT / FULFILLING / COMPLETED / CANCELLED */
    private String lifecyclePhase;
    private String payStatus;
    private Integer totalAmountCent;
    private Integer payAmountCent;
    private Integer deliveryFeeCent;
    private LocalDateTime expireAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
