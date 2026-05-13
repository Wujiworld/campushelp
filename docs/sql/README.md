# 数据库脚本说明

## 环境

- MySQL 8.0+
- 数据库：`campus_help`
- 字符集：`utf8mb4` / `utf8mb4_0900_ai_ci`

## 执行方式

在 MySQL 客户端执行（或 Navicat / Workbench 导入）。

应用默认 `spring.sql.init.mode=never`，不会在每次启动时自动执行脚本。建议首次部署手动执行，后续通过应用自身的种子机制管理。

## 推荐执行顺序

| 顺序 | 脚本 | 说明 |
| --- | --- | --- |
| 1 | `001_campus_help_schema_mysql8.sql` | 建表（全部业务表） |
| 2 | `002_campus_help_seed_basic.sql` | 基础数据（角色、权限等） |
| 3 | `005_campus_help_full_production_seed.sql` | 全量业务种子（校区、用户、门店、商品、SKU、示例订单） |
| 4 | `006_campus_help_life_seed.sql` | （可选）二手与活动抢票演示数据 |
| 5 | `007_campus_help_market_agent_comment.sql` | （可选）代购、评论、支付幂等 |
| 6 | `008_campus_help_like_profile.sql` | （可选）点赞、浏览量、地址与资料扩展 |
| 7 | `009_campus_help_notification_message.sql` | （可选）站内信/通知 |
| 8 | `009_ch_outbox.sql` | （可选）Outbox 模式事务消息表 |
| 9 | `010_campus_help_gap_fullfill.sql` | （可选）补充遗漏的索引/字段 |

## 测试账号

测试账号密码均为 **`123456`**（BCrypt 加密，与 Spring `BCryptPasswordEncoder` 一致）。详见 `005` 脚本内注释。

## 设计说明

### 统一订单
- `ch_order` + `ch_order_item`

### RBAC 权限
- `ch_user` + `ch_role` + `ch_user_role`

### 门店商品
- `ch_store`、`ch_product`、`ch_product_sku`

### 已移除的旧脚本
- `003` / `004`：已删除，数据由 `005` 统一提供，避免演示脚本与生产种子重复维护。

> 完整表结构说明见 `docs/CAPABILITIES_IMPLEMENTATION.md`。
