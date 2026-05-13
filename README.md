# campus-help（校园帮）

校园社区微服务平台，提供外卖点餐、二手交易、跑腿代购、活动发布、校园集市等一站式校园生活服务。

## 技术栈

| 层    | 技术                                                 |
| ---- | -------------------------------------------------- |
| 后端   | Java 17, Spring Boot 2.7.18, Spring Cloud 2021.0.8 |
| 数据库  | MySQL 8.0, MyBatis-Plus, Druid                     |
| 缓存   | Redis 7, Caffeine（三级缓存）, Redisson                  |
| 消息队列 | RabbitMQ（Direct/Topic/延迟队列）                        |
| 服务发现 | Nacos（可选）                                          |
| 限流熔断 | Sentinel（可选）                                       |
| 搜索   | Elasticsearch（可选）                                  |
| 前端   | React 19, TypeScript, Vite, Tailwind CSS           |
| 构建   | Maven, npm                                         |

## 模块架构

| 模块                           | 职责                         | 独立端口 |
| ---------------------------- | -------------------------- | ---- |
| `campus-help-server`         | **单体入口**（聚合所有服务）           | 8080 |
| `campus-help-gateway`        | API 网关，路由分发，流控             | 8081 |
| `campus-help-user`           | 用户服务（认证/权限/RBAC）           | 8082 |
| `campus-help-product`        | 商品/校园服务/门店/SKU             | 8084 |
| `campus-help-order`          | 订单服务（状态机/Outbox/支付）        | 8083 |
| `campus-help-life`           | 生活服务（秒杀/Feed流/评论/跑腿/集市/活动） | 8085 |
| `campus-help-search-indexer` | 搜索索引同步                     | 8091 |
| `campus-help-common`         | 公共组件（缓存/安全/事件/OSS/异常）      | —    |
| `campus-help-web`            | 前端页面                       | 5173 |

## 环境要求

- **JDK 17**+（推荐 Eclipse Temurin 或 Oracle JDK 17）
- **Maven** 3.6+
- **Docker** & **Docker Compose**（推荐启动中间件）
- **Node.js** 18+（仅前端开发需要）
- **IDE**：IntelliJ IDEA（推荐，需安装 Lombok 插件）

---

## 快速开始（5 分钟）

### 1. 克隆项目

```bash
git clone <仓库地址>
cd campus-help
```

### 2. 启动基础设施（Docker）

项目依赖 MySQL、Redis、RabbitMQ，推荐用 Docker Compose 一键启动：

```bash
docker compose up -d
```

启动后包含：

| 服务         | 端口           | 说明                                           |
| ---------- | ------------ | -------------------------------------------- |
| MySQL 8.0  | 3306         | 数据库 `campus_help`，用户 `root/root`             |
| Redis 7    | 6379         | 无密码                                          |
| RabbitMQ 3 | 5672 / 15672 | 用户 `guest/guest`，管理后台 http://localhost:15672 |

> 如果本机已安装 MySQL/Redis/RabbitMQ，可跳过此步，确保端口可用且配置匹配 `.env` 即可。

### 3. 配置环境变量

项目使用 `spring-dotenv` 自动加载 `.env` 文件：

```bash
# 复制示例配置
cp .env.example .env
```

`.env` 中最关键的配置项：

| 变量                     | 默认值                                           | 说明                          |
| ---------------------- | --------------------------------------------- | --------------------------- |
| `DB_URL`               | `jdbc:mysql://localhost:3306/campus_help?...` | MySQL 连接地址                  |
| `DB_USERNAME`          | `root`                                        | 数据库用户                       |
| `DB_PASSWORD`          | `233615`                                      | 数据库密码                       |
| `REDIS_HOST`           | `127.0.0.1`                                   | Redis 地址                    |
| `SPRING_RABBITMQ_HOST` | `127.0.0.1`                                   | RabbitMQ 地址                 |
| `JWT_SECRET`           | （内置开发密钥）                                      | JWT 签名密钥，生产环境请替换为 64 位随机字符串 |
| `OSS_ENABLED`          | `false`                                       | 阿里云 OSS 开关，开发环境不需要          |

> 开发环境保持默认即可运行。**生产环境务必**修改 `JWT_SECRET`、`DB_PASSWORD` 等敏感配置。

### 4. 初始化数据库

首次运行需要建表并导入种子数据。

**方式一：自动初始化（推荐）**

在 `.env` 中设置：

```properties
CAMPUS_SEED_ENABLED=true
CAMPUS_SEED_SCALE=small
CAMPUS_SEED_RESET=true
```

启动后端服务时会自动建表并写入演示数据。

**方式二：手动执行 SQL**

使用 MySQL 客户端按顺序执行：

```bash
# 建表
mysql -uroot -p campus_help < docs/sql/001_campus_help_schema_mysql8.sql

# 基础数据（角色、权限等）
mysql -uroot -p campus_help < docs/sql/002_campus_help_seed_basic.sql

# 全量业务种子（用户、门店、商品、SKU、示例订单）
mysql -uroot -p campus_help < docs/sql/005_campus_help_full_production_seed.sql
```

可选种子：

| 脚本                                         | 用途          |
| ------------------------------------------ | ----------- |
| `006_campus_help_life_seed.sql`            | 二手与活动抢票演示数据 |
| `007_campus_help_market_agent_comment.sql` | 代购、评论、支付幂等  |
| `008_campus_help_like_profile.sql`         | 点赞、浏览量      |
| `009_campus_help_notification_message.sql` | 站内信/通知      |

> 测试账号密码均为 `123456`（BCrypt 加密）。详见 `005` 脚本内注释。

### 5. 启动后端

#### 方式一：单体模式（推荐，开发使用）

所有业务模块聚合到 `campus-help-server` 一个进程运行，无需启动其他服务：

```bash
mvn clean install -DskipTests
cd campus-help-server
mvn spring-boot:run
```

或直接运行 `com.campushelp.server.CampusServerApplication`。

服务监听 `http://localhost:8080`。

#### 方式二：微服务模式（生产部署）

分别启动各服务（需搭配 Gateway）：

```bash
# 终端 1：用户服务
mvn -f campus-help-user/pom.xml spring-boot:run

# 终端 2：商品服务
mvn -f campus-help-product/pom.xml spring-boot:run

# 终端 3：订单服务
mvn -f campus-help-order/pom.xml spring-boot:run

# 终端 4：生活服务
mvn -f campus-help-life/pom.xml spring-boot:run

# 终端 5：网关
mvn -f campus-help-gateway/pom.xml spring-boot:run
```

> 微服务模式下通过 Gateway（8081）入口访问各服务。单体模式不需 Gateway。

### 6. 启动前端（可选）

```bash
cd campus-help-web
npm install
npm run dev
```

前端开发服务器监听 `http://localhost:5173`，Vite 自动将 `/api` 请求代理到 `http://localhost:8080`。

---

## 验证启动

| 端点                     | 说明    |
| ---------------------- | ----- |
| `GET /actuator/health` | 健康检查  |
| `GET /api/ping`        | 服务连通性 |

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

## 演示流程（外卖闭环）

启动后可按以下步骤测试完整的外卖下单流程。

### 1. 查门店

```http
GET http://localhost:8080/api/stores?campusId=1
```

### 2. 查商品

```http
GET http://localhost:8080/api/stores/{storeId}/products
```

### 3. 查 SKU

```http
GET http://localhost:8080/api/products/{productId}/skus
```

### 4. 创建订单

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "userId": 10001,
  "orderType": "TAKEOUT",
  "campusId": 1,
  "storeId": 1,
  "addressId": 1,
  "deliveryFeeCent": 300,
  "remark": "送到宿舍",
  "items": [
    {
      "skuId": 101,
      "title": "麻辣香锅大份",
      "unitPriceCent": 2999,
      "quantity": 1
    }
  ]
}
```

### 5. 模拟支付

```http
POST http://localhost:8080/api/orders/{id}/pay?userId=10001
```

### 6. 商家确认

```http
POST http://localhost:8080/api/orders/{id}/merchant/confirm?merchantUserId=30001
```

### 7. 骑手接单 → 取餐 → 配送 → 完成

```http
POST http://localhost:8080/api/orders/{id}/rider/take?riderUserId=20001
POST http://localhost:8080/api/orders/{id}/rider/pickup?riderUserId=20001
POST http://localhost:8080/api/orders/{id}/complete?userId=10001
```

### 8. 查看订单明细

```http
GET http://localhost:8080/api/orders/{id}/items
```

---

## 可选组件

### Nacos（服务发现与配置中心）

参考 `docs/nacos.md`，在 `.env` 中启用：

```properties
NACOS_ENABLED=true
NACOS_SERVER_ADDR=127.0.0.1:8848
```

启动 Nacos：

```bash
docker run -d -p 8848:8848 nacos/nacos-server:v2.2.3
```

### Sentinel（限流熔断）

参考 `docs/sentinel.md`，在 `.env` 中配置：

```properties
SENTINEL_DASHBOARD=127.0.0.1:8858
```

启动 Sentinel Dashboard：

```bash
docker run -d -p 8858:8858 bladex/sentinel-dashboard
```

### Elasticsearch（搜索）

启动 ES：

```bash
docker run -d -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.11.2
```

然后在 `.env` 中设置：

```properties
CAMPUS_SEARCH_PROVIDER=es
ELASTICSEARCH_URIS=http://127.0.0.1:9200
```

---

## IDEA 运行建议

1. **安装 Lombok 插件**：项目使用 `@Slf4j`、`@Data` 等注解
2. **开启注解处理**：`Settings → Build → Compiler → Annotation Processors → Enable annotation processing`
3. 单体开发直接运行 `CampusServerApplication`（`campus-help-server` 模块）
4. 微服务开发可同时运行多个 Application 实例（分别配置 `VM options: -Dserver.port=808X`）

---

## 参考文档

| 文档                   | 说明              |
| -------------------- | --------------- |
| `docs/gateway.md`    | API 网关架构与路由配置   |
| `docs/nacos.md`      | Nacos 服务发现与配置中心 |
| `docs/sentinel.md`   | Sentinel 限流熔断   |
| `docs/sql/README.md` | 数据库脚本说明         |
| `CLAUDE.md`          | 项目详细技术设计        |

## 常见问题

**Q：启动报 MySQL 连接失败？**

检查 MySQL 是否已启动以及 `.env` 中的 `DB_USERNAME`/`DB_PASSWORD` 是否正确。Docker 启动默认密码是 `root`。

**Q：端口冲突？**

各服务端口可通过环境变量修改，详见 `.env.example` 中的注释。

**Q：order-service 提示表不存在？**

单体模式下 `campus-help-server` 聚合了所有服务，首次启动会自动执行 `classpath:db/schema-order.sql`。如使用微服务模式，需先执行 SQL 脚本（步骤 4）。
