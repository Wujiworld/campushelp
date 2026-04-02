-- 仅包含 order-service 启动必需的最小表集合
-- 若你已执行 docs/sql/001_campus_help_schema_mysql8.sql，可把 spring.sql.init.mode 改为 never

CREATE TABLE IF NOT EXISTS ch_order (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_no         VARCHAR(32) NOT NULL COMMENT '业务订单号',
  order_type       VARCHAR(16) NOT NULL COMMENT 'TAKEOUT/ERRAND/SECONDHAND/TICKET',
  user_id          BIGINT UNSIGNED NOT NULL COMMENT '下单用户',
  store_id         BIGINT UNSIGNED NULL COMMENT '门店（外卖/代购）',
  merchant_user_id BIGINT UNSIGNED NULL COMMENT '商家账号',
  rider_user_id    BIGINT UNSIGNED NULL COMMENT '骑手账号（接单后填）',
  campus_id        BIGINT UNSIGNED NOT NULL,
  address_id       BIGINT UNSIGNED NULL COMMENT '配送地址（外卖/代购/代取）',
  status           VARCHAR(24) NOT NULL COMMENT 'CREATED/PAID/ACCEPTED/PREPARING/PICKED_UP/DELIVERING/COMPLETED/CANCELLED/REFUNDING/REFUNDED',
  pay_status       VARCHAR(16) NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/REFUNDING/REFUNDED',
  total_amount_cent INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总金额（分）',
  pay_amount_cent   INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '实付金额（分）',
  delivery_fee_cent INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送费（分）',
  remark           VARCHAR(255) NULL,
  expire_at        DATETIME(3) NULL COMMENT '支付过期时间',
  paid_at          DATETIME(3) NULL,
  cancelled_at     DATETIME(3) NULL,
  completed_at     DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user (user_id, created_at),
  KEY idx_order_status (status, created_at),
  KEY idx_order_expire (status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一订单主表';

-- 订单明细（V1：外卖/代购/抢票统一订单中心）
CREATE TABLE IF NOT EXISTS ch_order_item (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  item_type        VARCHAR(16) NOT NULL DEFAULT 'SKU' COMMENT 'SKU/SERVICE/TICKET/SECONDHAND',
  ref_id           BIGINT UNSIGNED NULL COMMENT '关联ID，如sku_id/票种id/二手商品id',
  title            VARCHAR(128) NOT NULL COMMENT '展示标题',
  unit_price_cent  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '单价（分）',
  quantity         INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '数量',
  amount_cent      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '小计（分）',
  snapshot_json    TEXT NULL COMMENT '下单快照(JSON字符串，避免后续变更影响历史订单)',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_oi_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

