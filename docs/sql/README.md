## 使用方式（MySQL 8）

在 MySQL 客户端执行（或用 Navicat/Workbench 导入）：

1) 创建库与表

```sql
SOURCE 001_campus_help_schema_mysql8.sql;
```

2) 初始化基础数据（角色/权限示例）

```sql
SOURCE 002_campus_help_seed_basic.sql;
```

## 设计说明（简版）

- **统一订单**：`ch_order` + `ch_order_item` + 各场景扩展表（`*_ext`），支撑外卖/代购代取/二手/抢票统一流转。
- **RBAC**：`ch_user` + `ch_role` + `ch_permission` + 关联表，支持学生/骑手/商家/管理员多角色隔离。
- **秒杀/抢票**：`ch_activity` + `ch_ticket_type` + `ch_activity_enroll`；高并发库存建议落在 Redis，数据库做结果落库与约束（`uk_enroll_user`）。

