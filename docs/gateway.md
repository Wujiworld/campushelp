# Gateway 网关层 · 完整文档

## 1. Gateway 是什么

Gateway 是整个系统的**唯一外部流量入口**，基于 Spring Cloud Gateway（WebFlux 响应式架构）构建。所有外部请求先到达 Gateway，再由 Gateway 按路径规则路由到后端的 user、order、product、life 等业务服务。

```java
客户端
  │
  ▼
┌─────────────────────────────────────────┐
│  Gateway (8081)                          │
│  ├─ 路由分发（Path Predicate）           │
│  ├─ Sentinel 网关流控                    │
│  └─ IP 限流 Key 解析器                  │
└──┬─────────┬─────────┬────────┬─────────┘
   │         │         │        │
   ▼         ▼         ▼        ▼
 user:8082 order:8083 prod:8084 life:8085
```

---

## 2. 项目中的 Gateway 架构

### 2.1 部署拓扑

```
                        ┌──────────────────┐
                        │   Nginx / LB     │  (可选，反向代理)
                        │   443 → 8081     │
                        └────────┬─────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │  campus-help-gateway    │
                    │  :8081                  │
                    │                         │
                    │  ┌──────────────────┐   │
                    │  │ Route: user      │   │  /api/v3/auth/**, /users/**, /addresses/**
                    │  │ → 127.0.0.1:8082 │   │
                    │  └──────────────────┘   │
                    │  ┌──────────────────┐   │
                    │  │ Route: order     │   │  /orders/**, /payments/**, /finance/**, ...
                    │  │ → 127.0.0.1:8083 │   │
                    │  └──────────────────┘   │
                    │  ┌──────────────────┐   │
                    │  │ Route: product   │   │  /stores/**, /products/**, /campuses/**
                    │  │ → 127.0.0.1:8084 │   │
                    │  └──────────────────┘   │
                    │  ┌──────────────────┐   │
                    │  │ Route: life      │   │  /activities/**, /seckill/**, /feed/**, ...
                    │  │ → 127.0.0.1:8085 │   │
                    │  └──────────────────┘   │
                    │  ┌──────────────────┐   │
                    │  │ Route: user-admin│   │  /admin/users/**, /admin/role-applications/**
                    │  │ → 127.0.0.1:8082 │   │
                    │  └──────────────────┘   │
                    └────────────────────────┘
```

### 2.2 请求处理链路

```
外部请求 POST /api/v3/orders
    │
    ▼
┌────────────────────────────────────────────┐
│ Gateway 接收 (Netty/WebFlux, :8081)         │
│ 提取请求路径: /api/v3/orders                │
└───────────────┬────────────────────────────┘
                │ Path Predicate 匹配
                ▼
┌────────────────────────────────────────────┐
│ 路由匹配: order-service                     │
│ 目标 URI: http://127.0.0.1:8083             │
└───────────────┬────────────────────────────┘
                │ Sentinel 网关流控检查
                ▼
┌────────────────────────────────────────────┐
│ Sentinel Gateway Filter                     │
│ 资源: route:order-service                   │
│ ┌────────────────────────────────────────┐  │
│ │ QPS ≤ 阈值 → 放行                       │  │
│ │ QPS > 阈值 → 返回 429 Too Many Requests │  │
│ └────────────────────────────────────────┘  │
└───────────────┬────────────────────────────┘
                │ 通过
                ▼
┌────────────────────────────────────────────┐
│ 转发到后端服务                               │
│ http://127.0.0.1:8083/api/v3/orders         │
│ ┌────────────────────────────────────────┐  │
│ │ OrderController.@SentinelResource       │  │
│ │ → 业务级流控检查                        │  │
│ │ → 业务逻辑执行                          │  │
│ └────────────────────────────────────────┘  │
└───────────────┬────────────────────────────┘
                │ 响应
                ▼
┌────────────────────────────────────────────┐
│ Gateway 将后端响应原样返回给客户端            │
│ 状态码 / Headers / Body 透传                │
└────────────────────────────────────────────┘
```

---

## 3. 项目依赖

```xml
<!-- campus-help-gateway/pom.xml -->
<project>
    <parent>
        <groupId>com.campushelp</groupId>
        <artifactId>campus-help</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>campus-help-gateway</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <!-- 核心：Spring Cloud Gateway（基于 WebFlux/Netty） -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <!-- 响应式 Redis 客户端（限流/令牌桶用） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>

        <!-- Redis 连接池 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>

        <!-- 健康检查端点 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Nacos 服务发现（可选，默认关闭） -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <!-- Sentinel 网关流控 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
    </dependencies>
</project>
```

**版本管理**（来自父 POM）：

| 依赖                   | 版本         |
| -------------------- | ---------- |
| Spring Boot          | 2.7.18     |
| Spring Cloud         | 2021.0.8   |
| Spring Cloud Alibaba | 2021.0.5.0 |
| JDK                  | 17         |

---

## 4. 核心代码实现

### 4.1 网关入口

**文件：** `campus-help-gateway/src/main/java/com/campushelp/gateway/CampusGatewayApplication.java`

```java
package com.campushelp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import reactor.core.publisher.Mono;

/**
 * API 网关入口：默认监听 8081，通过服务发现或静态路由转发到各业务微服务。
 */
@SpringBootApplication
public class CampusGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusGatewayApplication.java, args);
    }

    /**
     * 全局限流键：客户端 IP（配合 X-Forwarded-For 时由上游反向代理写入）。
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return Mono.just(xff.split(",")[0].trim());
            }
            var addr = exchange.getRequest().getRemoteAddress();
            String ip = addr != null ? addr.getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }
}
```

整个 Gateway 模块**仅有这一个 Java 文件**。无自定义 GlobalFilter、无认证拦截器、无 CORS 配置，采用"薄网关"设计哲学。

### 4.2 路由配置

**文件：** `campus-help-gateway/src/main/resources/application.yml`

```yaml
server:
  port: ${GATEWAY_PORT:8081}

spring:
  application:
    name: campus-help-gateway
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
  cloud:
    nacos:
      discovery:
        enabled: ${NACOS_ENABLED:false}
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
    sentinel:
      transport:
        dashboard: ${SENTINEL_DASHBOARD:127.0.0.1:8858}
        port: ${GATEWAY_SENTINEL_TRANSPORT_PORT:8720}
      eager: true
    gateway:
      routes:
        - id: user-service
          uri: ${GATEWAY_USER_URI:http://127.0.0.1:8082}
          predicates:
            - Path=/api/v3/auth/**,/api/v3/users/**,/api/v3 addresses/**
        - id: order-service
          uri: ${GATEWAY_ORDER_URI:http://127.0.0.1:8083}
          predicates:
            - Path=/api/v3/orders/**,/api/v3/payments/**,/api/v3/finance/**,/api/v3/merchant/refunds/**,/api/v3/admin/system/**,/api/v3/admin/refunds/**,/api/v3/admin/finance/**
        - id: product-service
          uri: ${GATEWAY_PRODUCT_URI:http://127.0.0.1:8084}
          predicates:
            - Path=/api/v3/stores/**,/api/v3/products/**,/api/v3/campuses/**
        - id: life-service
          uri: ${GATEWAY_LIFE_URI:http://127.0.0.1:8085}
          predicates:
            - Path=/api/v3/activities/**,/api/v3/agent/**,/api/v3/secondhand/**,/api/v3/market/**,/api/v3/comments/**,/api/v3/errands/**,/api/v3/messages/**,/api/v3/seckill/**,/api/v3/follows/**,/api/v3/likes/**,/api/v3/profile/**,/api/v3/feed/**,/api/v3/admin/content/**,/api/v3/admin/activities/**,/api/v3/admin/comments/**,/api/v3/admin/agent/**,/api/v3/admin/messages/**
        - id: user-admin-fallback
          uri: ${GATEWAY_USER_URI:http://127.0.0.1:8082}
          predicates:
            - Path=/api/v3/admin/users/**,/api/v3/admin/role-applications/**

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 4.3 路由速查表

| 路由 ID                 | 目标地址    | 路径前缀                                                                                                                                                                                                                                                                                           | 归属服务     |
| --------------------- | ------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| `user-service`        | `:8082` | `/auth/**`, `/users/**`, `/addresses/**`                                                                                                                                                                                                                                                       | 用户域      |
| `order-service`       | `:8083` | `/orders/**`, `/payments/**`, `/finance/**`, `/merchant/refunds/**`, `/admin/system/**`, `/admin/refunds/**`, `/admin/finance/**`                                                                                                                                                              | 订单域      |
| `product-service`     | `:8084` | `/stores/**`, `/products/**`, `/campuses/**`                                                                                                                                                                                                                                                   | 商品域      |
| `life-service`        | `:8085` | `/activities/**`, `/agent/**`, `/secondhand/**`, `/market/**`, `/comments/**`, `/errands/**`, `/messages/**`, `/seckill/**`, `/follows/**`, `/likes/**`, `/profile/**`, `/feed/**`, `/admin/content/**`, `/admin/activities/**`, `/admin/comments/**`, `/admin/agent/**`, `/admin/messages/**` | 生活域      |
| `user-admin-fallback` | `:8082` | `/admin/users/**`, `/admin/role-applications/**`                                                                                                                                                                                                                                               | 用户域(管理端) |

### 4.4 IP 限流 KeyResolver

`KeyResolver` 是 Spring Cloud Gateway 限流框架的扩展点，用于确定"以什么维度限流"。当前实现按**客户端 IP** 限流：

```
请求到达
    │
    ▼
ipKeyResolver() 解析
    │
    ├── 有 X-Forwarded-For 头 → 取第一个 IP（真实客户端）
    │   （适用于 Nginx/CDN 等反向代理场景）
    │
    └── 无 X-Forwarded-For → 取连接远程地址 IP
```

> 注意：当前 `KeyResolver` 已定义但**尚未被任何限流 Filter 引用**。预留用于未来启用 Redis 令牌桶限流。

---

## 5. 设计哲学：薄网关

Gateway 层遵循以下原则：

| 原则           | 体现                                         |
| ------------ | ------------------------------------------ |
| **只做路由**     | 5 条 Path 路由，无自定义 Filter，无业务逻辑              |
| **不处理认证**    | 无 JWT 校验、无 Token 拦截，认证委托给后端 `user-service` |
| **不处理 CORS** | 无 CORS 配置，由上游 Nginx 或后端服务处理                |
| **不处理限流**    | 已移除 Redis 令牌桶默认 Filter，统一使用 Sentinel       |
| **无状态**      | 不存储任何状态，可随时水平扩展                            |

```
Gateway 负责的事：
  ✓ 请求路由分发
  ✓ 网关级 Sentinel 流控
  ✓ 健康检查端点

Gateway 不负责的事：
  ✗ 用户认证/鉴权
  ✗ 业务逻辑
  ✗ CORS 跨域
  ✗ 请求/响应体修改
  ✗ 日志采集
```

---

## 6. Sentinel 网关流控

### 6.1 网关级 Sentinel 配置

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: ${SENTINEL_DASHBOARD:127.0.0.1:8858}
        port: ${GATEWAY_SENTINEL_TRANSPORT_PORT:8720}  # 与 Server 的 8719 区分
      eager: true    # 启动即注册，不等第一次请求
```

### 6.2 自动生成的网关资源

`spring-cloud-alibaba-sentinel-gateway` 依赖会自动为每条 route 注册 Sentinel 资源：

```
资源命名格式:
  route:{routeId}          # 例如 route:user-service
```

| 路由                  | Sentinel 资源名                |
| ------------------- | --------------------------- |
| user-service        | `route:user-service`        |
| order-service       | `route:order-service`       |
| product-service     | `route:product-service`     |
| life-service        | `route:life-service`        |
| user-admin-fallback | `route:user-admin-fallback` |

### 6.3 网关流控规则示例

在 Sentinel Dashboard 中为网关层配置流控规则：

```json
[
  {
    "resource": "route:life-service",
    "limitApp": "default",
    "grade": 1,
    "count": 2000,
    "strategy": 0,
    "controlBehavior": 0
  },
  {
    "resource": "route:order-service",
    "limitApp": "default",
    "grade": 1,
    "count": 1000,
    "strategy": 0,
    "controlBehavior": 0
  },
  {
    "resource": "route:product-service",
    "limitApp": "default",
    "grade": 1,
    "count": 1500,
    "strategy": 0,
    "controlBehavior": 0
  }
]
```

> 网关级流控 = 路由级别的全局限流；业务级 `@SentinelResource` = 接口级别的精细限流。两层可叠加使用。

---

## 7. 运行与部署

### 7.1 环境变量

```properties
# .env 文件
GATEWAY_PORT=8081

# 后端服务地址（可按需修改）
GATEWAY_USER_URI=http://127.0.0.1:8082
GATEWAY_ORDER_URI=http://127.0.0.1:8083
GATEWAY_PRODUCT_URI=http://127.0.0.1:8084
GATEWAY_LIFE_URI=http://127.0.0.1:8085

# Redis（网关 Sentinel 限流用）
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=

# 可选：Nacos 服务发现
NACOS_ENABLED=false
NACOS_SERVER_ADDR=127.0.0.1:8848

# 可选：Sentinel Dashboard
SENTINEL_DASHBOARD=127.0.0.1:8858
GATEWAY_SENTINEL_TRANSPORT_PORT=8720
```

### 7.2 启动

```bash
# 确保后端服务已启动
cd campus-help-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev &

# 启动 Gateway
cd campus-help-gateway
mvn spring-boot:run
```

### 7.3 验证

```bash
# 测试用户服务路由
curl http://127.0.0.1:8081/api/v3/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","smsCode":"123456"}'

# 测试商品服务路由
curl http://127.0.0.1:8081/api/v3/campuses

# 测试健康检查
curl http://127.0.0.1:8081/actuator/health
```

---

## 8. 进阶配置指南

### 8.1 切换为 Nacos 服务发现模式

当前使用静态 URI，如需改为服务发现动态路由：

**第一步：开启 Nacos 注册**

在 `.env` 中设置：

```properties
NACOS_ENABLED=true
```

**第二步：修改路由 URI 为 `lb://` 格式**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://campus-help-user        # lb = LoadBalancer
          predicates:
            - Path=/api/v3/auth/**,/api/v3/users/**,/api/v3/addresses/**
        - id: order-service
          uri: lb://campus-help-order
          predicates:
            - Path=/api/v3/orders/**,/api/v3/payments/**,/api/v3/finance/**
        - id: product-service
          uri: lb://campus-help-product
          predicates:
            - Path=/api/v3/stores/**,/api/v3/products/**,/api/v3/campuses/**
        - id: life-service
          uri: lb://campus-help-life
          predicates:
            - Path=/api/v3/activities/**,/api/v3/seckill/**,/api/v3/feed/**
```

**第三步：确保后端服务注册到 Nacos**

各后端模块需配置相同的 Nacos 地址：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: 127.0.0.1:8848
```

### 8.2 启用 Redis 令牌桶限流（预留能力）

当前已有 `KeyResolver` 和 Redis 依赖，启用只需在 `application.yml` 中添加：

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter:
              replenishRate: 10      # 每秒补充令牌数（稳态 QPS）
              burstCapacity: 20      # 令牌桶容量（允许突发）
              requestedTokens: 1     # 每次请求消耗令牌数
            key-resolver: "#{@ipKeyResolver}"
```

> 注意：如已在 Sentinel 层做流控，不建议同时启用双重限流。参见 `docs/sentinel.md` 中的对比分析。

### 8.3 添加 CORS 全局配置

如需在 Gateway 层处理跨域：

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns:
              - "https://*.campushelp.com"
              - "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### 8.4 添加全局日志 Filter

如需记录请求/响应日志，可新增 GlobalFilter：

```java
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        return chain.filter(exchange).doFinally(signal -> {
            long elapsed = System.currentTimeMillis() - start;
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
            log.info("[GATEWAY] {} {} {} {}ms", method, path, status, elapsed);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

### 8.5 添加统一认证 Filter

如需在网关层统一做 JWT 校验：

```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Value("${campus.gateway.auth.skip-paths:/api/v3/auth/**}")
    private List<String> skipPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 白名单路径跳过认证
        if (skipPaths.stream().anyMatch(p -> new AntPathMatcher().match(p, path))) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // TODO: JWT 校验，解析 userId 并写入 Header 传给下游
        // String userId = JwtUtils.parseUserId(token.substring(7));
        // exchange.mutate().request(r -> r.mutate()
        //     .header("X-User-Id", userId).build());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;  // 在路由之前执行
    }
}
```

---

## 9. 两种部署模式对比

| 维度            | 单体模式（当前默认）                 | 微服务模式                     |
| ------------- | -------------------------- | ------------------------- |
| 部署形态          | `campus-help-server` 单进程   | Gateway + 4 个独立服务进程       |
| Gateway 路由    | 路由到同一进程的不同端口               | 路由到独立进程                   |
| NACOS_ENABLED | `false`                    | `true`                    |
| 路由 URI        | 静态 `http://127.0.0.1:808X` | 动态 `lb://campus-help-xxx` |
| 服务发现          | 不需要                        | Nacos 自动注册/发现             |
| 适用场景          | 开发、测试、小规模部署                | 生产、需要水平扩展                 |

---

## 10. 常见问题

### Q: Gateway 层不做认证的原因是什么？

"薄网关"设计选择。认证逻辑涉及 Token 解析、用户信息查询、角色权限判断等，放在后端 `user-service` 中更合理。Gateway 只负责"请求该去哪"，业务安全由后端 `SecurityContextUtils` + `@RequireRole` 保证。

### Q: 为什么 life-service 的路由规则特别多？

life 域涵盖活动、秒杀、二手市场、Feed 流、评论、消息、跑腿等多个子功能，是当前业务最丰富的模块。如果后续业务膨胀，建议将 life 域进一步拆分为独立子网关。

### Q: Gateway 的 Redis 连接是必须的吗？

不是必须的。当前 Redis 仅用于 `KeyResolver` 的预留能力。如果不需要 Redis 令牌桶限流，可以不配置 Redis 连接（但 Sentinel 网关流控不依赖 Redis）。

### Q: 如何动态刷新路由规则？

当前路由规则写在 `application.yml` 中，修改后需重启。如需动态路由，可引入 Nacos Config（参见 `docs/nacos.md`），将路由配置移至 Nacos 管理，实现配置热更新。

### Q: Gateway 和 Sentinel Dashboard 的关系？

Gateway 是**流量入口**，Sentinel Dashboard 是**规则管理控制台**。Gateway 内置 Sentinel Agent，启动后自动向 Dashboard 注册。你在 Dashboard 配置的流控规则会实时推送到 Gateway 生效。
