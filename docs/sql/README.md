## 使用方式（MySQL 8）

在 MySQL 客户端执行（或 Navicat / Workbench 导入）。应用默认 **`spring.sql.init.mode=never`**，不会在每次启动时自动执行脚本。

### 推荐顺序（可上线全量数据）

1. **建表**  
   `001_campus_help_schema_mysql8.sql`

2. **基础数据（角色、权限等）**  
   `002_campus_help_seed_basic.sql`

3. **全量业务种子（校区、用户、门店、商品、SKU、示例订单）**  
   `005_campus_help_full_production_seed.sql`

4. **（可选）二手与活动抢票演示数据**  
   `006_campus_help_life_seed.sql`

5. **统一市场、代购、评论与支付幂等**  
   `007_campus_help_market_agent_comment.sql`（`ch_agent_item`、`ch_comment`、`ch_payment_notify`）

6. **站内信 / 通知（消息落库）**  
   `009_campus_help_notification_message.sql`（`ch_message`、`ch_message_recipient`）

7. **点赞、浏览量、地址与资料扩展**  
   `008_campus_help_like_profile.sql`（`ch_like`、`like_count` / `view_count` 等；需在对应业务库执行）

> 测试账号密码均为 **`123456`**（BCrypt，与 Spring `BCryptPasswordEncoder` 一致）。详见 `005` 脚本内注释。

### 已移除的旧脚本

- `003` / `004`：已删除，数据由 **`005`** 统一提供，避免「演示脚本」与「生产种子」重复维护。

## 设计说明（简版）

更完整的表说明见：`docs/DATABASE_ARCHITECTURE.md`。

- **统一订单**：`ch_order` + `ch_order_item`
- **RBAC**：`ch_user` + `ch_role` + `ch_user_role`
- **门店商品**：`ch_store`、`ch_product`、`ch_product_sku`
