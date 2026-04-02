USE campus_help;

-- 门店
INSERT INTO ch_store (id, merchant_user_id, campus_id, name, type, status, open_time, close_time, notice)
VALUES
  (1, 30001, 1, 'Canteen-1 Stall-1', 1, 1, '08:00', '20:00', 'Peak hours discount')
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

-- 商品（SPU）
INSERT INTO ch_product (id, store_id, name, cover_url, category, status)
VALUES
  (11, 1, 'Spicy Chicken Hotpot', NULL, 'Sichuan', 1),
  (12, 1, 'Tomato Beef', NULL, 'Rice Bowl', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

-- SKU
INSERT INTO ch_product_sku (id, product_id, sku_name, price_cent, stock, sold_count, status)
VALUES
  (101, 11, 'Large', 2999, 50, 0, 1),
  (102, 11, 'Small', 1999, 50, 0, 1),
  (103, 12, 'Single', 2599, 40, 0, 1),
  (104, 12, 'Double', 4999, 20, 0, 1)
ON DUPLICATE KEY UPDATE price_cent=VALUES(price_cent), stock=VALUES(stock), status=VALUES(status);

