# 校园帮 · 八大能力完整实现说明（代码索引与运维）

本文档对应「订单状态机 / 分布式关单与 Outbox / 领域事件 / 秒杀 / 安全 / 支付幂等与对账 / 流量治理 / 可观测」等能力在仓库中的**落地位置**、**配置项**与**部署依赖**，便于联调、面试陈述与二次开发。

---

## 1. 架构与进程

| 组件 | 说明 | 默认端口 |
|------|------|----------|
| `campus-help-server` | 单体业务（用户 / 订单 / 商品 / life） | 8080 |
| `campus-help-gateway` | Spring Cloud Gateway：边缘 Redis 令牌桶 + Sentinel 网关适配 | 8081 |
| MySQL / Redis / RabbitMQ | 数据与消息基础设施 | 按环境 |

推荐访问路径：**客户端 → Gateway(8081) → Server(8080)**；本地也可直连 8080 调试。

---

## 2. 订单状态机、CAS 与 SPI Hook

### 2.1 生命周期（DB 细粒度 + 视图聚合）

- 库表字段 `ch_order.status` / `pay_status`：`CREATED`、`PAID`、`MERCHANT_CONFIRMED`、`RIDER_TAKEN`、`DELIVERING`、`COMPLETED`、`CANCELLED` 等（见 [`OrderService`](../campus-help-order/src/main/java/com/campushelp/order/service/OrderService.java) 常量）。
- 对外聚合阶段 **`lifecyclePhase`**（面试口径 **FULFILLING**）：[`OrderLifecyclePhase`](../campus-help-order/src/main/java/com/campushelp/order/dto/OrderLifecyclePhase.java)，在 [`GET /api/v3/orders/{id}/summary`](../campus-help-order/src/main/java/com/campushelp/order/controller/OrderController.java) 的 [`OrderSummaryView`](../campus-help-order/src/main/java/com/campushelp/order/dto/OrderSummaryView.java) 中返回 `AWAITING_PAYMENT` / `FULFILLING` / `COMPLETED` / `CANCELLED`。

### 2.2 条件更新（乐观并发思想）

- **支付成功**：`WHERE id=? AND status=CREATED AND pay_status=UNPAID` 更新为已支付，失败则若已支付则幂等返回；票务再 CAS 到 `COMPLETED`（同文件 `applyPaySuccess`）。
- **超时关单**：`tryCloseSingleUnpaidOrder` 使用 `UpdateWrapper` 仅关 `CREATED+UNPAID`（[`OrderService`](../campus-help-order/src/main/java/com/campushelp/order/service/OrderService.java)）。

### 2.3 SPI / Hook（多业态扩展）

- 接口：[`OrderPaidSideEffect`](../campus-help-order/src/main/java/com/campushelp/order/spi/OrderPaidSideEffect.java)、[`OrderUnpaidClosedSideEffect`](../campus-help-order/src/main/java/com/campushelp/order/spi/OrderUnpaidClosedSideEffect.java)。
- 实现示例：[`life/orderhooks/*`](../campus-help-life/src/main/java/com/campushelp/life/orderhooks/TicketOrderHooks.java)（票务出票/释放库存）、[`TakeoutStockOrderHooks`](../campus-help-life/src/main/java/com/campushelp/life/orderhooks/TakeoutStockOrderHooks.java)、[`SecondhandOrderHooks`](../campus-help-life/src/main/java/com/campushelp/life/orderhooks/SecondhandOrderHooks.java)、[`AgentOrderHooks`](../campus-help-life/src/main/java/com/campushelp/life/orderhooks/AgentOrderHooks.java)。
- 票务支付 Hook **幂等**：同一 `order_id` 已存在报名则跳过插入（`TicketOrderHooks`）。

---

## 3. 关单双保险 + Outbox（本地消息表）

### 3.1 定时扫描

- [`OrderExpireScheduler`](../campus-help-order/src/main/java/com/campushelp/order/schedule/OrderExpireScheduler.java)，周期由 `campus.order.expire-scan-ms` 控制（[`application.yml`](../campus-help-server/src/main/resources/application.yml)）。

### 3.2 RabbitMQ `x-delayed-message`

- 配置：[`OrderDelayRabbitConfig`](../campus-help-life/src/main/java/com/campushelp/life/mq/OrderDelayRabbitConfig.java)，开关 `campus.mq.order-delay.enabled=true`。
- 投递：[`OrderDelayNotifierImpl`](../campus-help-life/src/main/java/com/campushelp/life/mq/OrderDelayNotifierImpl.java)；**所有待支付单创建** 经 [`OrderDelayNotifier.onUnpaidOrderCreated`](../campus-help-order/src/main/java/com/campushelp/order/spi/OrderDelayNotifier.java) 触发。
- 消费关单：[`OrderCloseUnpaidListener`](../campus-help-life/src/main/java/com/campushelp/life/mq/OrderCloseUnpaidListener.java) → `OrderService.tryCloseUnpaidOrder`。
- 支付超时时间：`campus.order.pay-timeout-minutes`（默认 15 分钟）。

### 3.3 Outbox（与业务同事务写入，异步投递 MQ）

- DDL：[`docs/sql/009_ch_outbox.sql`](sql/009_ch_outbox.sql)，并已合并进 [`schema-order.sql`](../campus-help-order/src/main/resources/db/schema-order.sql) 与 [`001_campus_help_schema_mysql8.sql`](sql/001_campus_help_schema_mysql8.sql)。
- 实体 / Mapper：[`ChOutbox`](../campus-help-order/src/main/java/com/campushelp/order/entity/ChOutbox.java)、[`ChOutboxMapper`](../campus-help-order/src/main/java/com/campushelp/order/mapper/ChOutboxMapper.java)。
- 有事务时写入 Outbox：[`RabbitDomainEventPublisher`](../campus-help-server/src/main/java/com/campushelp/server/event/RabbitDomainEventPublisher.java)；无事务仍直发：[`AmqpDomainEventSender`](../campus-help-server/src/main/java/com/campushelp/server/event/AmqpDomainEventSender.java)。
- 调度投递：[`OutboxPublishScheduler`](../campus-help-server/src/main/java/com/campushelp/server/event/OutboxPublishScheduler.java)，`campus.outbox.enabled`、`campus.outbox.publish-ms`。

**跨服务最终一致**：本仓库采用 **Outbox** 而非 Seata（单体 + MQ 场景更合适）；若需 Seata AT/TCC，需单独引入 TC 与数据源代理，不在此包内。

---

## 4. 领域事件、MQ、幂等、DLQ、WebSocket

- 事件模型：[`DomainEvent`](../campus-help-common/src/main/java/com/campushelp/common/event/DomainEvent.java)（`eventId` 幂等键）。
- 交换机 / 队列 / DLQ：[`EventBusConstants`](../campus-help-common/src/main/java/com/campushelp/common/event/EventBusConstants.java)、[`EventBusRabbitConfig`](../campus-help-server/src/main/java/com/campushelp/server/event/EventBusRabbitConfig.java)。
- 消费者：[`NotificationEventListener`](../campus-help-life/src/main/java/com/campushelp/life/notify/mq/NotificationEventListener.java) — **手动 ACK**、按 `event_id` 落库幂等、失败 `basicReject` 走 DLX。
- WebSocket：[`WebSocketConfig`](../campus-help-server/src/main/java/com/campushelp/server/ws/WebSocketConfig.java)、[`JwtHandshakeInterceptor`](../campus-help-server/src/main/java/com/campushelp/server/ws/JwtHandshakeInterceptor.java)。

---

## 5. 秒杀子系统

- Lua 原子预扣：[`SeckillTicketService`](../campus-help-life/src/main/java/com/campushelp/life/seckill/service/SeckillTicketService.java)（`DECR` 脚本）。
- Redisson 用户锁：`RLock tryLock` 防连点。
- MQ 异步下单：[`SeckillRabbitConfig`](../campus-help-life/src/main/java/com/campushelp/life/seckill/config/SeckillRabbitConfig.java)、[`SeckillOrderListener`](../campus-help-life/src/main/java/com/campushelp/life/seckill/listener/SeckillOrderListener.java)；失败 **`compensateStock`**。
- DB 条件更新兜底：[`ChTicketTypeMapper.tryReserveStock`](../campus-help-order/src/main/java/com/campushelp/order/mapper/ChTicketTypeMapper.java)。
- 库存预热：[`SeckillStockWarmupRunner`](../campus-help-life/src/main/java/com/campushelp/life/seckill/runner/SeckillStockWarmupRunner.java)。
- Sentinel 资源：`@SentinelResource` [`SeckillTicketController`](../campus-help-life/src/main/java/com/campushelp/life/seckill/controller/SeckillTicketController.java)。
- 压测示例脚本：[seckill-k6.js](perf/seckill-k6.js)（k6）；需在目标环境配置 JWT、票种 ID、`campus.seckill.enabled=true`。

---

## 6. 安全体系

- JWT + 状态less：[`JwtTokenProvider`](../campus-help-common/src/main/java/com/campushelp/common/security/JwtTokenProvider.java)、[`JwtAuthenticationFilter`](../campus-help-common/src/main/java/com/campushelp/common/security/JwtAuthenticationFilter.java)。
- RBAC + 方法级鉴权：[`@RequireRole`](../campus-help-common/src/main/java/com/campushelp/common/security/RequireRole.java)、[`RequireRoleAspect`](../campus-help-common/src/main/java/com/campushelp/common/security/RequireRoleAspect.java)。
- 水平越权：订单接口按 `userId` / 商家 / 骑手校验（如 [`OrderService`](../campus-help-order/src/main/java/com/campushelp/order/service/OrderService.java) `pay` / `cancel` / `complete` / `merchantConfirm` 等）。
- **网关级 HMAC + 时间戳**（可选）：[`ApiSignatureFilter`](../campus-help-common/src/main/java/com/campushelp/common/safety/ApiSignatureFilter.java)；配置 `campus.api-signature.*`（`enabled` / `secret` / `max-skew-seconds` / `path-prefixes`）；挂载于 [`CampusHelpWebSecurityConfig`](../campus-help-common/src/main/java/com/campushelp/common/config/CampusHelpWebSecurityConfig.java) 中 JWT 之前。
- 模拟支付头：`PaymentMockController` 仍支持 `X-Mock-Payment-Secret`（[`PaymentMockController`](../campus-help-order/src/main/java/com/campushelp/order/controller/PaymentMockController.java)）。

---

## 7. 支付与回调幂等、Redis 锁、对账

- 幂等表：`ch_payment_notify` 唯一索引 `pay_no`（见 [`007_campus_help_market_agent_comment.sql`](sql/007_campus_help_market_agent_comment.sql)）。
- 逻辑：[`OrderService.confirmPaidFromMock`](../campus-help-order/src/main/java/com/campushelp/order/service/OrderService.java) 先查后写、插入冲突吞并打日志。
- **Redis SETNX**（可选双轨）：`tryAcquirePayCallbackLock`，`campus.payment.callback-lock-ttl-seconds`（默认 0 关闭，避免无 Redis 开发环境报错；生产可设 `PAY_CALLBACK_LOCK_TTL`）。
- 对账骨架：[`PaymentReconcileScheduler`](../campus-help-server/src/main/java/com/campushelp/server/schedule/PaymentReconcileScheduler.java)，SQL [`ChOrderMapper`](../campus-help-order/src/main/java/com/campushelp/order/mapper/ChOrderMapper.java) `countPaidOrdersMissingSuccessNotify` / `countSuccessNotifyButOrderNotPaid`，周期 `campus.payment.reconcile-cron`。

---

## 8. 流量与风控

### 8.1 业务层（Servlet）— Redis + Lua

- [`RateLimitFilter`](../campus-help-common/src/main/java/com/campushelp/common/safety/RateLimitFilter.java)：
  - **`sliding`**：ZSET 滑动窗口（默认，配置 `campus.ratelimit.window-mode=sliding`）。
  - **`fixed`**：INCR 固定窗口（`window-mode=fixed`）。
- 开关：`campus.ratelimit.enabled`。

### 8.2 边缘层 — Spring Cloud Gateway

- 模块：[`campus-help-gateway`](../campus-help-gateway/pom.xml)。
- 全局 **`RequestRateLimiter`**（Redis 响应式 + 令牌桶）：`GATEWAY_RL_REPLENISH`、`GATEWAY_RL_BURST`（见 [`application.yml`](../campus-help-gateway/src/main/resources/application.yml)）。
- 后端 URI：`GATEWAY_BACKEND_URI`（默认 `http://127.0.0.1:8080`）。
- **Sentinel 网关**：依赖 `spring-cloud-alibaba-sentinel-gateway` + `spring-cloud-starter-alibaba-sentinel`，Dashboard `spring.cloud.sentinel.transport.dashboard`。

### 8.3 UGC 敏感词（Aho-Corasick）

- [`AhoCorasickAutomaton`](../campus-help-common/src/main/java/com/campushelp/common/safety/AhoCorasickAutomaton.java)、[`SensitiveWordService`](../campus-help-common/src/main/java/com/campushelp/common/safety/SensitiveWordService.java)，词表 [`sensitive-words.txt`](../campus-help-common/src/main/resources/sensitive-words.txt)，策略 `campus.safety.sensitive.mode=MASK|REJECT`。

---

## 9. 可观测与工程化

- **Prometheus**：[`campus-help-server`](../campus-help-server/pom.xml) 引入 `micrometer-registry-prometheus`，`management.endpoints.web.exposure.include` 含 `prometheus`（[`application.yml`](../campus-help-server/src/main/resources/application.yml)）。
- **SkyWalking**：无需改代码；示例 JVM 参数：  
  `-javaagent:/path/to/skywalking-agent/skywalking-agent.jar -DSW_AGENT_NAME=campus-help -DSW_AGENT_COLLECTOR_BACKEND_SERVICES=oap:11800`
- 统一响应与请求 ID：[`ApiResult`](../campus-help-common/src/main/java/com/campushelp/common/api/ApiResult.java)、[`RequestIdFilter`](../campus-help-common/src/main/java/com/campushelp/common/web/RequestIdFilter.java)、全局异常处理（`common/web/*`）。
- **OSS 预签名**：[`AliyunOssPresignService`](../campus-help-common/src/main/java/com/campushelp/common/oss/AliyunOssPresignService.java)、[`OssPresignController`](../campus-help-common/src/main/java/com/campushelp/common/oss/OssPresignController.java)。

---

## 10. 数据库迁移清单（按环境执行）

1. 主库结构：[`001_campus_help_schema_mysql8.sql`](sql/001_campus_help_schema_mysql8.sql)（已含 `ch_outbox`）。
2. 增量：[`007_*.sql`](sql/007_campus_help_market_agent_comment.sql) 等历史脚本。
3. Outbox 独立脚本：[`009_ch_outbox.sql`](sql/009_ch_outbox.sql)（若已执行 001 最新版可跳过）。

---

## 11. 关键环境变量 / 配置一览

| 配置键 | 含义 |
|--------|------|
| `campus.order.pay-timeout-minutes` | 未支付超时（分钟） |
| `campus.mq.order-delay.enabled` | 是否启用延迟关单 MQ |
| `campus.outbox.enabled` / `campus.outbox.publish-ms` | Outbox 与投递间隔 |
| `campus.ratelimit.window-mode` | `sliding` / `fixed` |
| `campus.payment.callback-lock-ttl-seconds` | 支付回调 Redis 锁 TTL，0=关闭 |
| `campus.api-signature.enabled` | 是否校验 HMAC 签名 |
| `GATEWAY_BACKEND_URI` / `GATEWAY_PORT` | 网关后端与端口 |
| `campus.seckill.enabled` | 秒杀接口开关 |

---

## 12. 运行顺序建议

1. 启动 MySQL、Redis、RabbitMQ（含 **delayed message 插件** 若使用延迟交换机）。
2. 执行 DDL / 迁移。
3. 启动 **`campus-help-server`**（8080）。
4. 可选：启动 **`campus-help-gateway`**（8081），并将对外入口指向网关。
5. 打开 Sentinel Dashboard（可选），为网关或资源配置流控规则。

---

*文档版本与代码库同步维护；若增加新模块或配置，请在本文件补充对应小节与路径。*
