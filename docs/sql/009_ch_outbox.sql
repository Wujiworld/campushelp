-- 领域事件本地 Outbox（与业务同事务写入，异步投递 Rabbit）
USE campus_help;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ch_outbox (
  id            BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  event_id      VARCHAR(64) NOT NULL COMMENT '与 DomainEvent.eventId 一致，防重复入库',
  payload_json  TEXT NOT NULL COMMENT 'DomainEvent JSON',
  created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  published_at  DATETIME(3) NULL COMMENT '已成功发往 MQ 的时间',
  UNIQUE KEY uk_outbox_event_id (event_id),
  KEY idx_outbox_pending (published_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表 Outbox';
