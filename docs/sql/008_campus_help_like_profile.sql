-- 点赞 + 二手/活动展示计数（执行前请确认库名）
USE campus_help;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ch_like (
  id               BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  user_id          BIGINT UNSIGNED NOT NULL,
  target_type      VARCHAR(32) NOT NULL COMMENT 'SECONDHAND_ITEM / ACTIVITY',
  target_id        BIGINT UNSIGNED NOT NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_like_user_target (user_id, target_type, target_id),
  KEY idx_like_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户点赞（收藏）';

-- 兼容已存在库：仅当列不存在时追加
SET @db := DATABASE();
SET @t1 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ch_secondhand_item' AND COLUMN_NAME = 'like_count'
);
SET @sql1 := IF(@t1 = 0,
  'ALTER TABLE ch_secondhand_item ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''点赞数'' AFTER status',
  'SELECT 1');
PREPARE s1 FROM @sql1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @t2 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ch_activity' AND COLUMN_NAME = 'like_count'
);
SET @sql2 := IF(@t2 = 0,
  'ALTER TABLE ch_activity ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''点赞数'' AFTER status',
  'SELECT 1');
PREPARE s2 FROM @sql2; EXECUTE s2; DEALLOCATE PREPARE s2;

SET @t3 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ch_secondhand_item' AND COLUMN_NAME = 'view_count'
);
SET @sql3 := IF(@t3 = 0,
  'ALTER TABLE ch_secondhand_item ADD COLUMN view_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT ''浏览量'' AFTER like_count',
  'SELECT 1');
PREPARE s3 FROM @sql3; EXECUTE s3; DEALLOCATE PREPARE s3;
