-- =============================================================================
-- 009: 通知系统（站内信 + 推送幂等）
-- =============================================================================

-- 站内信消息主体（按 eventId 幂等）
CREATE TABLE IF NOT EXISTS ch_message (
    id BIGINT PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    type VARCHAR(64) NOT NULL,
    biz_id VARCHAR(64) NULL,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(512) NOT NULL,
    payload_json TEXT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_ch_message_event_id (event_id),
    KEY idx_ch_message_type_created_at (type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 站内信收件箱（用户维度已读/软删）
CREATE TABLE IF NOT EXISTS ch_message_recipient (
    id BIGINT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at DATETIME NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_ch_message_recipient_msg_user (message_id, user_id),
    KEY idx_ch_message_recipient_user_created_at (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

