# 联动脚本与可观测落地（Micrometer + SkyWalking）

## 1. 目标
- 一次演示同时覆盖：
  - Feed 推拉链路（发布 -> 异步 fan-out -> 时间线读取）
  - 搜索查询链路（`MarketQueryService` 指标）
  - 多级缓存链路（`CacheFacade` 命中指标）
  - 端到端观测（Micrometer 指标 + SkyWalking Trace）

## 2. 启动顺序
- 后端服务：
  - `campus-help-user` (`8082`)
  - `campus-help-order` (`8083`)
  - `campus-help-product` (`8084`)
  - `campus-help-life` (`8085`)
  - `campus-help-gateway` (`8081`)
  - `campus-help-search-indexer` (`8091`)
- 依赖：
  - MySQL、Redis、RabbitMQ
  - 可选：SkyWalking OAP + UI

## 3. 一键联动脚本
- 路径：`scripts/demo-linkage-observability.ps1`
- 执行示例：

```powershell
pwsh ./scripts/demo-linkage-observability.ps1
```

- 带 SkyWalking 提示的执行方式：

```powershell
pwsh ./scripts/demo-linkage-observability.ps1 -WithSkyWalking
```

脚本会依次做：
- 健康检查（网关 + 各服务）
- 登录拿 JWT
- Feed 发布 + 时间线读取
- 触发市场搜索请求
- 抓取关键 Micrometer 指标端点

## 4. Micrometer 指标检查点
确保已暴露 actuator 指标端点（`life/search-indexer` 已配置）：
- `http://127.0.0.1:8085/actuator/metrics/campus.feed.publish`
- `http://127.0.0.1:8085/actuator/metrics/campus.feed.timeline.read`
- `http://127.0.0.1:8085/actuator/metrics/campus.feed.fanout`
- `http://127.0.0.1:8085/actuator/metrics/campus.search.query.total`
- `http://127.0.0.1:8085/actuator/metrics/campus.cache.hit`
- `http://127.0.0.1:8091/actuator/metrics/campus.search.reconcile.run`

Prometheus 抓取地址：
- `http://127.0.0.1:8085/actuator/prometheus`
- `http://127.0.0.1:8091/actuator/prometheus`

## 5. SkyWalking 接入模板
当前工程已具备 `requestId`、事件流和指标埋点；SkyWalking 通过 Java Agent 接入最稳妥。

### 5.1 Agent 启动参数（示例）
在每个服务启动前设置 `MAVEN_OPTS`（或 IDEA VM Options）：

```powershell
$env:MAVEN_OPTS='-javaagent:D:/tools/skywalking/agent/skywalking-agent.jar -Dskywalking.agent.service_name=campus-help-life -Dskywalking.collector.backend_service=127.0.0.1:11800'
```

按服务名分别改 `service_name`：
- `campus-help-gateway`
- `campus-help-user`
- `campus-help-order`
- `campus-help-product`
- `campus-help-life`
- `campus-help-search-indexer`

### 5.2 Trace 验证路径
在 SkyWalking UI 中查看：
- Endpoint 包含 `/api/v3/feed/publish`、`/api/v3/feed/timeline`
- Endpoint 包含 `/api/v3/market/search`
- 调用链预期：
  - `gateway -> life`
  - `life -> redis`
  - `life -> rabbitmq`（Feed 事件）
  - `search-indexer` 消费 MQ 后写 ES（接入后可见）

## 6. 演示话术（简版）
- “我们先通过脚本触发真实业务读写，再直接看指标增长与 trace 路径，证明链路可观测。”
- “Feed 发布和读取对应 `campus.feed.*` 指标，缓存命中走 `campus.cache.hit`，搜索链路走 `campus.search.query.total`。”
- “SkyWalking 里看到网关到业务服务再到中间件的 span，说明全链路 Trace 打通。”

## 7. 常见问题
- 指标 404：
  - 检查服务是否重启过（新配置生效）
  - 检查 `management.endpoints.web.exposure.include` 是否包含 `metrics,prometheus`
- SkyWalking 无数据：
  - 检查 OAP 地址和端口
  - 检查 `-javaagent` 路径和服务 `service_name`
  - 确认先有真实请求流量再看拓扑
