USE campus_help;

-- 角色资质审核申请
CREATE TABLE IF NOT EXISTS ch_role_application (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  role_code        VARCHAR(32) NOT NULL COMMENT 'RIDER/MERCHANT',
  doc_type         VARCHAR(64) NULL,
  doc_no           VARCHAR(128) NULL,
  doc_images_json  JSON NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  submit_remark    VARCHAR(255) NULL,
  audit_remark     VARCHAR(255) NULL,
  audited_by       BIGINT UNSIGNED NULL,
  audited_at       DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_role_app_user (user_id, created_at),
  KEY idx_role_app_status (status, created_at),
  KEY idx_role_app_role (role_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色资质审核申请';

-- 退款申请与审核
CREATE TABLE IF NOT EXISTS ch_order_refund (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  user_id          BIGINT UNSIGNED NOT NULL,
  merchant_user_id BIGINT UNSIGNED NULL,
  apply_reason     VARCHAR(255) NOT NULL,
  apply_amount_cent INT UNSIGNED NOT NULL DEFAULT 0,
  status           VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  audit_by         BIGINT UNSIGNED NULL,
  audit_role       VARCHAR(16) NULL COMMENT 'MERCHANT/ADMIN',
  audit_remark     VARCHAR(255) NULL,
  audited_at       DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_refund_order_user (order_id, user_id),
  KEY idx_refund_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单退款申请';

-- 结算台账
CREATE TABLE IF NOT EXISTS ch_settlement_ledger (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  user_id          BIGINT UNSIGNED NOT NULL COMMENT '收益归属用户（商家/骑手）',
  user_role        VARCHAR(16) NOT NULL COMMENT 'MERCHANT/RIDER',
  amount_cent      INT NOT NULL COMMENT '正数入账，负数冲销',
  status           VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/FROZEN/SETTLED',
  biz_type         VARCHAR(32) NOT NULL COMMENT 'ORDER_INCOME/REFUND_ROLLBACK/WITHDRAW',
  biz_id           BIGINT UNSIGNED NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_settle_user (user_id, user_role, created_at),
  KEY idx_settle_order (order_id),
  KEY idx_settle_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算台账';

CREATE TABLE IF NOT EXISTS ch_withdraw_apply (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  user_role        VARCHAR(16) NOT NULL COMMENT 'MERCHANT/RIDER',
  amount_cent      INT UNSIGNED NOT NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/PAID',
  account_no       VARCHAR(128) NOT NULL,
  account_name     VARCHAR(64) NOT NULL,
  remark           VARCHAR(255) NULL,
  audit_by         BIGINT UNSIGNED NULL,
  audit_remark     VARCHAR(255) NULL,
  audited_at       DATETIME(3) NULL,
  paid_at          DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_withdraw_user (user_id, user_role, created_at),
  KEY idx_withdraw_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现申请';

-- 关注关系
CREATE TABLE IF NOT EXISTS ch_user_follow (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  follower_user_id BIGINT UNSIGNED NOT NULL,
  followee_user_id BIGINT UNSIGNED NOT NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_follow_pair (follower_user_id, followee_user_id),
  KEY idx_followee (followee_user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系';

-- 管理配置
CREATE TABLE IF NOT EXISTS ch_system_config (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  config_key       VARCHAR(64) NOT NULL,
  config_value     VARCHAR(512) NOT NULL,
  updated_by       BIGINT UNSIGNED NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_sys_cfg_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

CREATE TABLE IF NOT EXISTS ch_audit_log (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  operator_user_id BIGINT UNSIGNED NULL,
  operator_role    VARCHAR(16) NULL,
  action           VARCHAR(64) NOT NULL,
  target_type      VARCHAR(32) NOT NULL,
  target_id        VARCHAR(64) NULL,
  detail           VARCHAR(512) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_audit_op (operator_user_id, created_at),
  KEY idx_audit_action (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键操作审计日志';
