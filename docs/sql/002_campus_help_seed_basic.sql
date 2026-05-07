-- 基础种子数据：角色/部分权限示例（可按需扩展）
USE campus_help;

-- 角色
INSERT INTO ch_role (id, code, name, status)
VALUES
  (1001, 'STUDENT',  '学生',  1),
  (1002, 'RIDER',    '骑手',  1),
  (1003, 'MERCHANT', '商家',  1),
  (1004, 'ADMIN',    '管理员', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

-- 权限点（示例：只放最小可跑通链路）
INSERT INTO ch_permission (id, code, name, type, method, uri_pattern, status)
VALUES
  (2001, 'auth:login',          '登录', 1, 'POST', '/api/v3/auth/login', 1),
  (2101, 'order:create',        '下单', 1, 'POST', '/api/v3/orders', 1),
  (2102, 'order:pay',           '支付', 1, 'POST', '/api/v3/orders/*/pay', 1),
  (2103, 'order:cancel',        '取消订单', 1, 'POST', '/api/v3/orders/*/cancel', 1),
  (2201, 'rider:grab',          '骑手抢单', 1, 'POST', '/api/rider/orders/*/grab', 1),
  (2301, 'merchant:confirm',    '商家确认接单/出餐', 1, 'POST', '/api/v3/orders/*/merchant/confirm', 1),
  (2401, 'admin:activity:publish','发布活动', 1, 'POST', '/api/admin/activities/*/publish', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), type=VALUES(type), method=VALUES(method), uri_pattern=VALUES(uri_pattern), status=VALUES(status);

-- 角色-权限（示例）
INSERT INTO ch_role_permission (id, role_id, permission_id)
VALUES
  (3001, 1001, 2101),
  (3002, 1001, 2102),
  (3003, 1001, 2103),
  (3004, 1002, 2201),
  (3005, 1003, 2301),
  (3006, 1004, 2401)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), permission_id=VALUES(permission_id);
