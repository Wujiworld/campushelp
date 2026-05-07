package com.campushelp.common.event;

/**
 * 本期通知系统事件类型（可持续扩展）。
 */
public enum NotificationEventType {
    // -------- Order --------
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_MERCHANT_CONFIRMED,
    ORDER_RIDER_TAKEN,
    ORDER_RIDER_PICKUP,
    ORDER_COMPLETED,
    ORDER_CANCELLED,
    ORDER_UNPAID_CLOSED,

    // -------- Comment --------
    COMMENT_CREATED,
    COMMENT_DELETED,
    COMMENT_HIDDEN,

    // -------- Activity --------
    ACTIVITY_PUBLISHED,
    ACTIVITY_SOLD_OUT,
    ACTIVITY_ENROLL_SUCCESS,
    ACTIVITY_ENROLL_CANCELLED,

    // -------- Social --------
    FOLLOW_CREATED,
    FEED_POST_PUBLISHED,

    // -------- System --------
    SYSTEM_ANNOUNCEMENT
}

