-- 代购条目 + 评论（在 001/002/005 之后执行；可与 008 点赞脚本独立执行）
USE campus_help;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ch_agent_item (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  seller_user_id   BIGINT UNSIGNED NOT NULL,
  campus_id        BIGINT UNSIGNED NOT NULL,
  title            VARCHAR(128) NOT NULL,
  description      VARCHAR(2048) NULL,
  price_cent       INT UNSIGNED NOT NULL COMMENT '标价（分）',
  status           VARCHAR(16) NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/OFFLINE',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_agent_seller (seller_user_id, created_at),
  KEY idx_agent_campus (campus_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园代购条目';

CREATE TABLE IF NOT EXISTS ch_comment (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  target_type      VARCHAR(32) NOT NULL COMMENT 'ACTIVITY/SECONDHAND_ITEM/AGENT_ITEM/STORE',
  target_id        BIGINT UNSIGNED NOT NULL,
  content          VARCHAR(512) NOT NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE/HIDDEN',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_comment_target (target_type, target_id, created_at),
  KEY idx_comment_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多目标评论';

CREATE TABLE IF NOT EXISTS ch_payment_notify (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  pay_no           VARCHAR(64) NOT NULL COMMENT '渠道支付单号/幂等键',
  status           VARCHAR(16) NOT NULL COMMENT 'SUCCESS 等',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_payment_pay_no (pay_no),
  KEY idx_payment_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调幂等记录';
