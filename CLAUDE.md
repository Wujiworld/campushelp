# Campus Help — 校园社区微服务平台

## 项目概述
- **名称**: campus-help
- **技术栈**: Java 17, Spring Boot 2.7.18, Spring Cloud, MyBatis-Plus, MySQL, Redis, RabbitMQ, Elasticsearch
- **架构**: 微服务架构，8个模块
- **JDK**: 17（确保使用 JDK 17 语法和 API）

## 模块结构

| 模块 | 职责 | 端口 |
|------|------|------|
| campus-help-gateway | API 网关，路由转发，签名校验 | 8080 |
| campus-help-common | 公共组件（缓存/安全/事件/OSS/异常） | — |
| campus-help-user | 用户服务（认证/权限） | 8081 |
| campus-help-product | 商品/校园服务 | 8082 |
| campus-help-order | 订单服务（状态机/Outbox/支付） | 8083 |
| campus-help-life | 生活服务（秒杀/Feed流/评论/跑腿/Agent） | 8084 |
| campus-help-search-indexer | 搜索索引同步 | 8085 |
| campus-help-server | 聚合服务（Outbox调度/支付对账/WebSocket） | 8086 |
| campus-help-activity | 活动服务 | — |

## 核心设计

### 缓存架构
- 三级缓存：Caffeine(L1) → Redis(L2) → DB(L3)
- CacheFacade 门面封装，互斥锁防缓存击穿，随机 TTL 防雪崩
- Micrometer 埋点按层统计命中率

### 事务消息（Outbox 模式）
- 业务事务内 INSERT Outbox 表
- OutboxPublishScheduler 每 1s 扫描未投递记录（LIMIT 100）
- RabbitMQ 投递到下游消费者

### 订单状态机
- 状态流转：CREATED → PAID → SHIPPED → COMPLETED
- SQL 条件更新 `UPDATE ... WHERE status=?` 防并发状态漂移
- 关单双保险：定时任务 + RabbitMQ 延迟队列

### 秒杀系统
- Redis Lua 原子扣库存（DECR + 预检）
- Redisson 分布式锁防止超卖
- MQ 异步下单，延迟队列关单兜底

### Feed 流
- 普通用户：写扩散（推送至粉丝 Inbox，Sorted Set）
- 大 V：拉模式（按分片合并读取）
- 游标分页（cursor-based pagination）

### 安全体系
- JWT（RS256）生成与校验
- @RequireRole 注解 + AOP 切面实现 RBAC
- 网关层 API 签名验签
- Aho-Corasick 敏感词过滤

## 技术债/待办
- OutboxPublishScheduler 缺 afterCommit 直接发送（纯扫表模式）
- 秒杀消费端缺幂等校验
- MQ 发送失败缺库存补偿
- SearchReconcileScheduler 是全量对账空壳（只自增计数）
- 各服务缺单元测试
- 缺线程池自定义配置（@Scheduled 默认单线程）
- 缺 Docker Compose 一键部署
- GitHub README 未完善
- 缺压测数据报告

## 开发规范
- Java 17, Spring Boot 2.7.18
- 包路径: com.campushelp.*
- API 统一返回格式: ApiResult<T>
- 异常统一处理: GlobalExceptionHandler
- 数据库: MyBatis-Plus + MySQL
- MQ: RabbitMQ（Direct/Topic/Delayed 交换机）
- 构建: Maven
- 控制器命名: *Controller.java
- 服务接口命名: *Service.java
- 实体命名: *Entity.java

## 常用命令
| 命令 | 用途 |
|------|------|
| mvn clean install -DskipTests | 构建全部模块 |
| cd campus-help-server && mvn spring-boot:run | 启动聚合服务 |
| cd campus-help-gateway && mvn spring-boot:run | 启动网关 |
