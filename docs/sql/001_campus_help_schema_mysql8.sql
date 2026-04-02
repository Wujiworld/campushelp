-- 校园帮 | 校园一站式生活服务平台
-- MySQL 8.x / InnoDB / utf8mb4
-- 说明：
-- 1) 面向微服务，默认“不建外键”，通过业务保证一致性；但保留了清晰的字段命名以便后续加 FK。
-- 2) 统一使用 bigint unsigned 作为主键（建议雪花 ID / 号段）。
-- 3) 金额统一用分（int unsigned）避免精度问题。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS campus_help
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE campus_help;

-- =========================
-- 0. 基础：校区/楼栋/地址
-- =========================

CREATE TABLE IF NOT EXISTS ch_campus (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  code             VARCHAR(32) NOT NULL COMMENT '校区编码',
  name             VARCHAR(64) NOT NULL COMMENT '校区名称',
  city             VARCHAR(32) NULL,
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_campus_code (code),
  KEY idx_campus_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区';

CREATE TABLE IF NOT EXISTS ch_building (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  campus_id        BIGINT UNSIGNED NOT NULL,
  name             VARCHAR(64) NOT NULL COMMENT '楼栋/宿舍区名称',
  type             TINYINT NOT NULL DEFAULT 1 COMMENT '1宿舍 2教学楼 3办公楼 4其他',
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_building_campus (campus_id),
  KEY idx_building_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋/区域';

CREATE TABLE IF NOT EXISTS ch_address (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  campus_id        BIGINT UNSIGNED NOT NULL,
  building_id      BIGINT UNSIGNED NULL,
  contact_name     VARCHAR(32) NOT NULL,
  contact_phone    VARCHAR(32) NOT NULL,
  detail           VARCHAR(255) NOT NULL COMMENT '详细地址，如寝室号/楼层/门牌',
  label            VARCHAR(32) NULL COMMENT '家/宿舍/公司等',
  is_default       TINYINT NOT NULL DEFAULT 0 COMMENT '1默认 0否',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_addr_user (user_id),
  KEY idx_addr_campus (campus_id),
  KEY idx_addr_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货/服务地址';

-- =========================
-- 1. 用户与 RBAC（多角色）
-- =========================

CREATE TABLE IF NOT EXISTS ch_user (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  phone            VARCHAR(32) NOT NULL COMMENT '手机号（登录账号）',
  password_hash    VARCHAR(128) NULL COMMENT '密码hash（可选：短信登录时为空）',
  nickname         VARCHAR(32) NULL,
  avatar_url       VARCHAR(255) NULL,
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  campus_id        BIGINT UNSIGNED NULL COMMENT '主校区（可空）',
  last_login_at    DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_user_phone (phone),
  KEY idx_user_status (status),
  KEY idx_user_campus (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户（统一账号体系）';

CREATE TABLE IF NOT EXISTS ch_role (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  code             VARCHAR(32) NOT NULL COMMENT '角色编码：STUDENT/RIDER/MERCHANT/ADMIN',
  name             VARCHAR(64) NOT NULL,
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_role_code (code),
  KEY idx_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS ch_permission (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  code             VARCHAR(64) NOT NULL COMMENT '权限点，如 order:create / rider:grab',
  name             VARCHAR(64) NOT NULL,
  type             TINYINT NOT NULL DEFAULT 1 COMMENT '1接口 2菜单 3按钮',
  parent_id        BIGINT UNSIGNED NULL COMMENT '菜单树父节点',
  path             VARCHAR(128) NULL COMMENT '前端路由/菜单路径（菜单类权限使用）',
  method           VARCHAR(16) NULL COMMENT 'HTTP方法（接口权限使用）',
  uri_pattern      VARCHAR(255) NULL COMMENT 'URI模式（接口权限使用）',
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_perm_code (code),
  KEY idx_perm_parent (parent_id),
  KEY idx_perm_type (type),
  KEY idx_perm_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点（接口/菜单/按钮）';

CREATE TABLE IF NOT EXISTS ch_user_role (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  role_id          BIGINT UNSIGNED NOT NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

CREATE TABLE IF NOT EXISTS ch_role_permission (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  role_id          BIGINT UNSIGNED NOT NULL,
  permission_id    BIGINT UNSIGNED NOT NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  KEY idx_rp_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限';

-- 角色扩展档案（可选：便于存不同角色特有字段）
CREATE TABLE IF NOT EXISTS ch_rider_profile (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  real_name        VARCHAR(32) NULL,
  student_no       VARCHAR(32) NULL COMMENT '校内骑手可能为学生',
  id_card_no       VARCHAR(32) NULL,
  vehicle_type     TINYINT NOT NULL DEFAULT 1 COMMENT '1电动车 2自行车 3步行 4其他',
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1可接单 0停用',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_rider_user (user_id),
  KEY idx_rider_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骑手档案';

CREATE TABLE IF NOT EXISTS ch_merchant_profile (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  merchant_name    VARCHAR(64) NOT NULL COMMENT '商家主体/店主名称',
  license_no       VARCHAR(64) NULL COMMENT '营业执照号（可选）',
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0停用',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_merchant_user (user_id),
  KEY idx_merchant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家档案';

-- =========================
-- 2. 商家/门店/商品（外卖、代购共用）
-- =========================

CREATE TABLE IF NOT EXISTS ch_store (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  merchant_user_id BIGINT UNSIGNED NOT NULL COMMENT '归属商家账号',
  campus_id        BIGINT UNSIGNED NOT NULL,
  name             VARCHAR(64) NOT NULL,
  type             TINYINT NOT NULL DEFAULT 1 COMMENT '1食堂档口 2校外商家 3超市 4快递点 5其他',
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1营业 0打烊 -1停用',
  open_time        VARCHAR(16) NULL COMMENT '如 09:00',
  close_time       VARCHAR(16) NULL COMMENT '如 21:30',
  notice           VARCHAR(255) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_store_merchant (merchant_user_id),
  KEY idx_store_campus (campus_id),
  KEY idx_store_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店/档口';

CREATE TABLE IF NOT EXISTS ch_product (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  store_id         BIGINT UNSIGNED NOT NULL,
  name             VARCHAR(64) NOT NULL,
  cover_url        VARCHAR(255) NULL,
  category         VARCHAR(32) NULL,
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_product_store (store_id),
  KEY idx_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU';

CREATE TABLE IF NOT EXISTS ch_product_sku (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  product_id       BIGINT UNSIGNED NOT NULL,
  sku_name         VARCHAR(64) NULL COMMENT '规格名，如 大份/加辣',
  price_cent       INT UNSIGNED NOT NULL COMMENT '单价（分）',
  stock            INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '普通库存（非秒杀）',
  sold_count       INT UNSIGNED NOT NULL DEFAULT 0,
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '1可售 0不可售',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_sku_product (product_id),
  KEY idx_sku_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';

-- =========================
-- 3. 统一订单中心（主表 + 明细 + 扩展表）
-- =========================

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
  expire_at        DATETIME(3) NULL COMMENT '支付过期时间（用于超时取消）',
  paid_at          DATETIME(3) NULL,
  cancelled_at     DATETIME(3) NULL,
  completed_at     DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_user (user_id, created_at),
  KEY idx_order_store (store_id, created_at),
  KEY idx_order_merchant (merchant_user_id, created_at),
  KEY idx_order_rider (rider_user_id, created_at),
  KEY idx_order_status (status, created_at),
  KEY idx_order_type (order_type, created_at),
  KEY idx_order_expire (status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一订单主表';

CREATE TABLE IF NOT EXISTS ch_order_item (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  item_type        VARCHAR(16) NOT NULL DEFAULT 'SKU' COMMENT 'SKU/SERVICE/TICKET/SECONDHAND',
  ref_id           BIGINT UNSIGNED NULL COMMENT '关联ID，如sku_id/票种id/二手商品id',
  title            VARCHAR(128) NOT NULL COMMENT '展示标题',
  unit_price_cent  INT UNSIGNED NOT NULL DEFAULT 0,
  quantity         INT UNSIGNED NOT NULL DEFAULT 1,
  amount_cent      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '小计（分）',
  snapshot_json    JSON NULL COMMENT '下单快照（可选）',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_oi_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- 订单扩展：外卖
CREATE TABLE IF NOT EXISTS ch_order_takeout_ext (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  pickup_point     VARCHAR(128) NULL COMMENT '取餐点/档口窗口描述（可选）',
  expect_arrive_at DATETIME(3) NULL COMMENT '期望送达时间（可选）',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_takeout_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖订单扩展';

-- 订单扩展：代购/代取
CREATE TABLE IF NOT EXISTS ch_order_errand_ext (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  errand_type      VARCHAR(16) NOT NULL COMMENT 'BUY/EXPRESS_PICKUP/DOCUMENT_DELIVERY/OTHER',
  pickup_address   VARCHAR(255) NULL COMMENT '取货地址（如快递点/超市）',
  pickup_code      VARCHAR(64) NULL COMMENT '取件码（代取快递）',
  list_text        VARCHAR(1024) NULL COMMENT '代购清单（简版）',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_errand_order (order_id),
  KEY idx_errand_type (errand_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代购代取订单扩展';

-- 订单扩展：二手交易
CREATE TABLE IF NOT EXISTS ch_order_secondhand_ext (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  secondhand_item_id BIGINT UNSIGNED NOT NULL,
  delivery_mode    VARCHAR(16) NOT NULL DEFAULT 'MEETUP' COMMENT 'MEETUP当面/DELIVERY配送',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_sh_order (order_id),
  KEY idx_sh_item (secondhand_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手订单扩展';

-- 订单扩展：活动抢票/报名
CREATE TABLE IF NOT EXISTS ch_order_ticket_ext (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  activity_id      BIGINT UNSIGNED NOT NULL,
  ticket_type_id   BIGINT UNSIGNED NOT NULL COMMENT '票种/场次',
  entry_code       VARCHAR(64) NULL COMMENT '入场码（可选，核销用）',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_ticket_order (order_id),
  KEY idx_ticket_activity (activity_id),
  KEY idx_ticket_type (ticket_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动抢票订单扩展';

-- 订单状态流转日志（便于追踪/对账/幂等）
CREATE TABLE IF NOT EXISTS ch_order_status_log (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  from_status      VARCHAR(24) NOT NULL,
  to_status        VARCHAR(24) NOT NULL,
  operator_type    VARCHAR(16) NOT NULL COMMENT 'SYSTEM/STUDENT/RIDER/MERCHANT/ADMIN',
  operator_id      BIGINT UNSIGNED NULL COMMENT '操作人user_id（SYSTEM可空）',
  reason           VARCHAR(255) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_osl_order (order_id, created_at),
  KEY idx_osl_to (to_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态日志';

-- =========================
-- 4. 履约（骑手接单/配送任务）
-- =========================

CREATE TABLE IF NOT EXISTS ch_delivery_task (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  rider_user_id    BIGINT UNSIGNED NULL COMMENT '接单后填',
  mode             VARCHAR(16) NOT NULL DEFAULT 'GRAB' COMMENT 'GRAB抢单/ASSIGN派单',
  status           VARCHAR(16) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/TAKEN/PICKED_UP/DELIVERING/DONE/CANCELLED',
  taken_at         DATETIME(3) NULL,
  picked_up_at     DATETIME(3) NULL,
  done_at          DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_task_order (order_id),
  KEY idx_task_rider (rider_user_id, created_at),
  KEY idx_task_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送任务';

-- 抢单记录（可选：用于风控/统计）
CREATE TABLE IF NOT EXISTS ch_grab_record (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  order_id         BIGINT UNSIGNED NOT NULL,
  rider_user_id    BIGINT UNSIGNED NOT NULL,
  result           TINYINT NOT NULL COMMENT '1成功 0失败',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_grab_order (order_id, created_at),
  KEY idx_grab_rider (rider_user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢单记录';

-- =========================
-- 5. 二手交易（商品发布/图片）
-- =========================

CREATE TABLE IF NOT EXISTS ch_secondhand_item (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  seller_user_id   BIGINT UNSIGNED NOT NULL,
  campus_id        BIGINT UNSIGNED NOT NULL,
  title            VARCHAR(128) NOT NULL,
  description      VARCHAR(2048) NULL,
  price_cent       INT UNSIGNED NOT NULL COMMENT '标价（分）',
  negotiable       TINYINT NOT NULL DEFAULT 1 COMMENT '1可议价 0不议价',
  status           VARCHAR(16) NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/SOLD/OFFLINE/AUDITING/REJECTED',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_sh_seller (seller_user_id, created_at),
  KEY idx_sh_campus (campus_id, created_at),
  KEY idx_sh_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品';

CREATE TABLE IF NOT EXISTS ch_secondhand_image (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  item_id          BIGINT UNSIGNED NOT NULL,
  url              VARCHAR(255) NOT NULL,
  sort_no          INT UNSIGNED NOT NULL DEFAULT 0,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_sh_img_item (item_id, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手商品图片';

-- =========================
-- 6. 活动抢票/秒杀（活动、票种、报名）
-- =========================

CREATE TABLE IF NOT EXISTS ch_activity (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  campus_id        BIGINT UNSIGNED NOT NULL,
  title            VARCHAR(128) NOT NULL,
  description      VARCHAR(2048) NULL,
  place            VARCHAR(255) NULL COMMENT '地点',
  start_time       DATETIME(3) NOT NULL,
  end_time         DATETIME(3) NOT NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE/ENDED',
  created_by       BIGINT UNSIGNED NULL COMMENT '管理员user_id',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_act_campus (campus_id, start_time),
  KEY idx_act_status (status, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动';

CREATE TABLE IF NOT EXISTS ch_ticket_type (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  activity_id      BIGINT UNSIGNED NOT NULL,
  name             VARCHAR(64) NOT NULL COMMENT '票种/场次名称',
  price_cent       INT UNSIGNED NOT NULL DEFAULT 0,
  stock_total      INT UNSIGNED NOT NULL DEFAULT 0,
  stock_sold       INT UNSIGNED NOT NULL DEFAULT 0,
  per_user_limit   INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '单用户限购/限报',
  sale_start_time  DATETIME(3) NOT NULL,
  sale_end_time    DATETIME(3) NOT NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'ON' COMMENT 'ON/OFF',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY idx_tt_activity (activity_id),
  KEY idx_tt_time (sale_start_time, sale_end_time),
  KEY idx_tt_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票种/场次（可做秒杀）';

CREATE TABLE IF NOT EXISTS ch_activity_enroll (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  activity_id      BIGINT UNSIGNED NOT NULL,
  ticket_type_id   BIGINT UNSIGNED NOT NULL,
  user_id          BIGINT UNSIGNED NOT NULL,
  order_id         BIGINT UNSIGNED NULL COMMENT '若报名走订单则关联',
  status           VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/CANCELLED/REFUNDED',
  entry_code       VARCHAR(64) NULL COMMENT '核销码/入场码',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_enroll_user (user_id, ticket_type_id),
  KEY idx_enroll_activity (activity_id, created_at),
  KEY idx_enroll_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名/抢票结果';

-- =========================
-- 7. 支付（先支持模拟支付/第三方回调）
-- =========================

CREATE TABLE IF NOT EXISTS ch_payment (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  pay_no           VARCHAR(32) NOT NULL COMMENT '支付单号',
  order_id         BIGINT UNSIGNED NOT NULL,
  order_no         VARCHAR(32) NOT NULL,
  user_id          BIGINT UNSIGNED NOT NULL,
  channel          VARCHAR(16) NOT NULL DEFAULT 'MOCK' COMMENT 'MOCK/WECHAT/ALIPAY',
  amount_cent      INT UNSIGNED NOT NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/SUCCESS/FAIL/CLOSED/REFUNDING/REFUNDED',
  request_id       VARCHAR(64) NULL COMMENT '幂等请求号',
  paid_at          DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_pay_no (pay_no),
  UNIQUE KEY uk_pay_req (request_id),
  KEY idx_pay_order (order_id),
  KEY idx_pay_user (user_id, created_at),
  KEY idx_pay_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';

-- =========================
-- 8. 通知/站内信（WebSocket 可配合使用）
-- =========================

CREATE TABLE IF NOT EXISTS ch_message (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  biz_type         VARCHAR(32) NOT NULL COMMENT 'ORDER/ACTIVITY/SYSTEM',
  biz_id           BIGINT UNSIGNED NULL COMMENT '关联业务ID，如order_id/activity_id',
  title            VARCHAR(64) NOT NULL,
  content          VARCHAR(1024) NOT NULL,
  read_flag        TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY idx_msg_user (user_id, read_flag, created_at),
  KEY idx_msg_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信/通知';

-- =========================
-- 9. 幂等与风控（可选：落库版）
-- =========================

CREATE TABLE IF NOT EXISTS ch_idempotency_record (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  idem_key         VARCHAR(128) NOT NULL COMMENT '幂等Key（建议包含userId+biz+uuid）',
  user_id          BIGINT UNSIGNED NULL,
  biz_type         VARCHAR(32) NOT NULL,
  biz_id           BIGINT UNSIGNED NULL,
  status           VARCHAR(16) NOT NULL DEFAULT 'LOCKED' COMMENT 'LOCKED/SUCCESS/FAILED',
  expire_at        DATETIME(3) NOT NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_idem_key (idem_key),
  KEY idx_idem_expire (expire_at),
  KEY idx_idem_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='幂等记录（可选，通常用Redis即可）';

SET FOREIGN_KEY_CHECKS = 1;
