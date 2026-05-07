-- 校园帮 · 上线级全量种子数据（开发/预发/演示环境）
-- 前提：已执行 001_campus_help_schema_mysql8.sql、002_campus_help_seed_basic.sql
-- 默认密码（所有下列测试账号一致）：123456  — BCrypt 与 Spring BCryptPasswordEncoder 兼容
-- 可重复执行：主键冲突处使用 ON DUPLICATE KEY UPDATE

USE campus_help;

SET NAMES utf8mb4;

-- 统一 BCrypt("123456")
SET @pwd := '$2a$10$fjuBodlRHJOue6DiOAoC1uir2nqtm8kgCZ976eiIZjKP1nu28B5PW';

-- ========== 校区 / 楼栋 ==========
INSERT INTO ch_campus (id, code, name, city, status, created_at, updated_at) VALUES
  (1, 'MAIN', '主校区（东区）', '示例市', 1, NOW(3), NOW(3)),
  (2, 'WEST', '西校区', '示例市', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW(3);

INSERT INTO ch_building (id, campus_id, name, type, status, created_at, updated_at) VALUES
  (101, 1, '梧桐苑 1 号楼', 1, 1, NOW(3), NOW(3)),
  (102, 1, '梧桐苑 2 号楼', 1, 1, NOW(3), NOW(3)),
  (103, 1, '第一教学楼', 2, 1, NOW(3), NOW(3)),
  (201, 2, '银杏园 A 座', 1, 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW(3);

-- ========== 用户（学生 / 骑手 / 商家）==========
INSERT INTO ch_user (id, phone, password_hash, nickname, status, campus_id, created_at, updated_at) VALUES
  (10001, '13800010001', @pwd, '张同学', 1, 1, NOW(3), NOW(3)),
  (10002, '13800010002', @pwd, '李同学', 1, 1, NOW(3), NOW(3)),
  (10003, '13800010003', @pwd, '王同学', 1, 1, NOW(3), NOW(3)),
  (10004, '13800010004', @pwd, '赵同学', 1, 1, NOW(3), NOW(3)),
  (10005, '13800010005', @pwd, '钱同学', 1, 1, NOW(3), NOW(3)),
  (10006, '13800010006', @pwd, '孙同学', 1, 2, NOW(3), NOW(3)),
  (10007, '13800010007', @pwd, '周同学', 1, 2, NOW(3), NOW(3)),
  (10008, '13800010008', @pwd, '吴同学', 1, 1, NOW(3), NOW(3)),
  (10009, '13800010009', @pwd, '郑同学', 1, 1, NOW(3), NOW(3)),
  (10010, '13800010010', @pwd, '冯同学', 1, 1, NOW(3), NOW(3)),
  (20001, '13900020001', @pwd, '骑手小刘', 1, 1, NOW(3), NOW(3)),
  (20002, '13900020002', @pwd, '骑手小陈', 1, 1, NOW(3), NOW(3)),
  (20003, '13900020003', @pwd, '骑手小杨', 1, 1, NOW(3), NOW(3)),
  (20004, '13900020004', @pwd, '骑手小周', 1, 2, NOW(3), NOW(3)),
  (20005, '13900020005', @pwd, '骑手小吴', 1, 2, NOW(3), NOW(3)),
  (30001, '13700030001', @pwd, '食堂张老板', 1, 1, NOW(3), NOW(3)),
  (30002, '13700030002', @pwd, '奶茶李老板', 1, 1, NOW(3), NOW(3)),
  (30003, '13700030003', @pwd, '超市王老板', 1, 2, NOW(3), NOW(3)),
  (99901, '13900009999', @pwd, '系统管理员（测试）', 1, 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  password_hash = VALUES(password_hash),
  status = VALUES(status),
  campus_id = VALUES(campus_id),
  updated_at = NOW(3);

-- 角色：1001 STUDENT 1002 RIDER 1003 MERCHANT
INSERT INTO ch_user_role (id, user_id, role_id, created_at) VALUES
  (60001, 10001, 1001, NOW(3)), (60002, 10002, 1001, NOW(3)), (60003, 10003, 1001, NOW(3)),
  (60004, 10004, 1001, NOW(3)), (60005, 10005, 1001, NOW(3)), (60006, 10006, 1001, NOW(3)),
  (60007, 10007, 1001, NOW(3)), (60008, 10008, 1001, NOW(3)), (60009, 10009, 1001, NOW(3)),
  (60010, 10010, 1001, NOW(3)),
  (61001, 20001, 1002, NOW(3)), (61002, 20002, 1002, NOW(3)), (61003, 20003, 1002, NOW(3)),
  (61004, 20004, 1002, NOW(3)), (61005, 20005, 1002, NOW(3)),
  (62001, 30001, 1003, NOW(3)), (62002, 30002, 1003, NOW(3)), (62003, 30003, 1003, NOW(3)),
  (63001, 99901, 1004, NOW(3))
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ========== 收货地址 ==========
INSERT INTO ch_address (id, user_id, campus_id, building_id, contact_name, contact_phone, detail, label, is_default, created_at, updated_at) VALUES
  (1, 10001, 1, 101, '张同学', '13800010001', '301 室', '宿舍', 1, NOW(3), NOW(3)),
  (2, 10002, 1, 102, '李同学', '13800010002', '612 室', '宿舍', 1, NOW(3), NOW(3)),
  (3, 10006, 2, 201, '孙同学', '13800010006', '1208', '宿舍', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE detail = VALUES(detail), updated_at = NOW(3);

-- ========== 门店 / 商品 / SKU ==========
INSERT INTO ch_store (id, merchant_user_id, campus_id, name, type, status, open_time, close_time, notice, created_at, updated_at) VALUES
  (1, 30001, 1, '第一食堂 · 川味档口', 1, 1, '07:00', '21:00', '今日推荐：麻辣香锅', NOW(3), NOW(3)),
  (2, 30001, 1, '第一食堂 · 面食档口', 1, 1, '07:00', '20:30', '牛肉面热销', NOW(3), NOW(3)),
  (3, 30002, 1, '梧桐奶茶站', 3, 1, '09:00', '22:00', '第二杯半价（指定款）', NOW(3), NOW(3)),
  (4, 30002, 1, '自习室咖啡角', 3, 1, '08:00', '21:00', '美式现磨', NOW(3), NOW(3)),
  (5, 30003, 2, '西校生活超市', 3, 1, '08:00', '23:00', '满 39 元配送宿舍区', NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE name = VALUES(name), notice = VALUES(notice), status = VALUES(status), updated_at = NOW(3);

INSERT INTO ch_product (id, store_id, name, cover_url, category, status, created_at, updated_at) VALUES
  (11, 1, '麻辣香锅套餐', NULL, '热菜', 1, NOW(3), NOW(3)),
  (12, 1, '番茄牛腩饭', NULL, '米饭', 1, NOW(3), NOW(3)),
  (13, 1, '酸辣粉', NULL, '小吃', 1, NOW(3), NOW(3)),
  (21, 2, '红烧牛肉面', NULL, '面食', 1, NOW(3), NOW(3)),
  (22, 2, '葱油拌面', NULL, '面食', 1, NOW(3), NOW(3)),
  (31, 3, '珍珠奶茶', NULL, '饮品', 1, NOW(3), NOW(3)),
  (32, 3, '杨枝甘露', NULL, '饮品', 1, NOW(3), NOW(3)),
  (41, 4, '美式咖啡', NULL, '咖啡', 1, NOW(3), NOW(3)),
  (51, 5, '桶装泡面组合', NULL, '速食', 1, NOW(3), NOW(3)),
  (52, 5, '牛奶 1L', NULL, '乳品', 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updated_at = NOW(3);

INSERT INTO ch_product_sku (id, product_id, sku_name, price_cent, stock, sold_count, status, created_at, updated_at) VALUES
  (101, 11, '大份·加肉', 1899, 200, 12, 1, NOW(3), NOW(3)),
  (102, 11, '标准份', 1499, 200, 30, 1, NOW(3), NOW(3)),
  (103, 12, '单人餐', 1699, 150, 8, 1, NOW(3), NOW(3)),
  (104, 13, '标准', 1299, 100, 5, 1, NOW(3), NOW(3)),
  (201, 21, '大碗', 1599, 80, 4, 1, NOW(3), NOW(3)),
  (202, 22, '标准', 999, 120, 10, 1, NOW(3), NOW(3)),
  (301, 31, '大杯', 1299, 300, 50, 1, NOW(3), NOW(3)),
  (302, 32, '中杯', 1599, 200, 20, 1, NOW(3), NOW(3)),
  (401, 41, '热', 999, 150, 40, 1, NOW(3), NOW(3)),
  (501, 51, '三连包', 1599, 500, 0, 1, NOW(3), NOW(3)),
  (502, 52, '1L', 1299, 200, 0, 1, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE price_cent = VALUES(price_cent), stock = VALUES(stock), status = VALUES(status), updated_at = NOW(3);

-- ========== 示例订单（多状态，便于联调）==========
INSERT INTO ch_order (
  id, order_no, order_type, user_id, store_id, merchant_user_id, rider_user_id, campus_id, address_id,
  status, pay_status, total_amount_cent, pay_amount_cent, delivery_fee_cent, remark,
  expire_at, paid_at, cancelled_at, completed_at, created_at, updated_at
) VALUES
  (90001, 'CH2026040309001', 'TAKEOUT', 10001, 1, 30001, 20001, 1, 1,
   'COMPLETED', 'PAID', 2198, 2198, 300, '历史已完成单',
   NULL, DATE_SUB(NOW(3), INTERVAL 2 DAY), NULL, DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_SUB(NOW(3), INTERVAL 3 DAY), NOW(3)),
  (90002, 'CH2026040309002', 'TAKEOUT', 10002, 3, 30002, NULL, 1, 2,
   'MERCHANT_CONFIRMED', 'PAID', 1599, 1599, 200, '待骑手接单',
   NULL, DATE_SUB(NOW(3), INTERVAL 5 MINUTE), NULL, NULL, DATE_SUB(NOW(3), INTERVAL 10 MINUTE), NOW(3)),
  (90003, 'CH2026040309003', 'TAKEOUT', 10003, 1, NULL, NULL, 1, 1,
   'CREATED', 'UNPAID', 1798, 0, 300, '待支付',
   DATE_ADD(NOW(3), INTERVAL 20 MINUTE), NULL, NULL, NULL, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  pay_status = VALUES(pay_status),
  remark = VALUES(remark),
  updated_at = NOW(3);

INSERT INTO ch_order_item (id, order_id, item_type, ref_id, title, unit_price_cent, quantity, amount_cent, snapshot_json, created_at) VALUES
  (90101, 90001, 'SKU', 102, '麻辣香锅套餐 标准份', 1499, 1, 1499, '{}', DATE_SUB(NOW(3), INTERVAL 3 DAY)),
  (90102, 90002, 'SKU', 301, '珍珠奶茶 大杯', 1299, 1, 1299, '{}', DATE_SUB(NOW(3), INTERVAL 10 MINUTE)),
  (90103, 90003, 'SKU', 101, '麻辣香锅套餐 大份·加肉', 1899, 1, 1899, '{}', NOW(3))
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  unit_price_cent = VALUES(unit_price_cent),
  quantity = VALUES(quantity),
  amount_cent = VALUES(amount_cent);
