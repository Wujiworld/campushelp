-- 校园帮 · 二手 / 活动抢票 演示种子（可选，在 005 之后执行）
-- 前提：001 + 002 + 005
USE campus_help;
SET NAMES utf8mb4;

-- 二手（主校区，在售）
INSERT INTO ch_secondhand_item (id, seller_user_id, campus_id, title, description, price_cent, negotiable, status, created_at, updated_at) VALUES
  (71001, 10003, 1, '九成新 线性代数教材', '无笔记，封面轻微折痕', 1500, 1, 'ON_SALE', NOW(3), NOW(3)),
  (71002, 10004, 1, '宿舍用小台灯', 'USB 供电，三档调光', 3500, 1, 'ON_SALE', NOW(3), NOW(3)),
  (71003, 10002, 1, '蓝牙键盘', '罗技 K380 同款成色', 8900, 0, 'ON_SALE', NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW(3);

INSERT INTO ch_secondhand_image (id, item_id, url, sort_no, created_at) VALUES
  (71501, 71001, 'https://placehold.co/400x300/e2e8f0/64748b?text=Book', 0, NOW(3)),
  (71502, 71002, 'https://placehold.co/400x300/fef3c7/b45309?text=Lamp', 0, NOW(3)),
  (71503, 71003, 'https://placehold.co/400x300/dcfce7/166534?text=Keyboard', 0, NOW(3))
ON DUPLICATE KEY UPDATE url = VALUES(url);

-- 活动 + 票种（售票窗口放宽，便于联调）
INSERT INTO ch_activity (id, campus_id, title, description, place, start_time, end_time, status, created_by, created_at, updated_at) VALUES
  (82001, 1, '春季校园音乐节', '操场露天，请提前入场', '东操场',
   DATE_ADD(NOW(3), INTERVAL 7 DAY), DATE_ADD(DATE_ADD(NOW(3), INTERVAL 7 DAY), INTERVAL 3 HOUR),
   'PUBLISHED', 10001, NOW(3), NOW(3)),
  (82002, 1, '职业规划分享会', '校友分享 + 答疑', '图书馆报告厅',
   DATE_ADD(NOW(3), INTERVAL 14 DAY), DATE_ADD(DATE_ADD(NOW(3), INTERVAL 14 DAY), INTERVAL 2 HOUR),
   'PUBLISHED', 10001, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status), updated_at = NOW(3);

INSERT INTO ch_ticket_type (id, activity_id, name, price_cent, stock_total, stock_sold, per_user_limit, sale_start_time, sale_end_time, status, created_at, updated_at) VALUES
  (83001, 82001, '早鸟票', 0, 200, 0, 2, DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_ADD(NOW(3), INTERVAL 30 DAY), 'ON', NOW(3), NOW(3)),
  (83002, 82001, '普通票', 500, 500, 0, 2, DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_ADD(NOW(3), INTERVAL 30 DAY), 'ON', NOW(3), NOW(3)),
  (83003, 82002, '入场券', 100, 100, 0, 1, DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_ADD(NOW(3), INTERVAL 30 DAY), 'ON', NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE name = VALUES(name), stock_total = VALUES(stock_total), status = VALUES(status), updated_at = NOW(3);
