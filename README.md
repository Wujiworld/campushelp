## campus-help（校园帮）| 第一步跑起来

### 1) 启动 MySQL（推荐：Docker）

在 `D:/work/MYproject/campus-help/` 目录执行：

```bash
docker compose up -d
```

> 如果你用的是本机已安装的 MySQL，就只需要确保：
> - `localhost:3306` 可访问
> - 账号密码与你的 `DB_USERNAME/DB_PASSWORD` 一致（默认 `root/root`）

数据库信息（默认）：
- db: `campus_help`
- user: `root`
- password: `root`
- port: `3306`

### 2) 启动订单服务（order-service）

```bash
mvn -f campus-help-order/pom.xml spring-boot:run
```

如果你的 MySQL 不是默认配置，可以在命令行临时覆盖：

```bash
DB_URL="jdbc:mysql://你的ip或域名:3306/campus_help?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true" `
DB_USERNAME="xxx" `
DB_PASSWORD="yyy" `
mvn -f campus-help-order/pom.xml spring-boot:run
```

服务端口：`8081`

### 3) 验证是否启动成功

- 健康检查：`GET /actuator/health`
- 连通性：`GET /api/ping`

### 4) V1 外卖闭环（演示）

#### 4.1 查门店/商品/SKU（product-service：8082）

门店：
```http
GET http://localhost:8082/api/stores?campusId=1
```

店内商品：
```http
GET http://localhost:8082/api/stores/{storeId}/products
```

商品 SKU：
```http
GET http://localhost:8082/api/products/{productId}/skus
```

#### 4.2 下单（order-service：8081）

创建订单（`userId/订单各角色ID` 暂未接登录，由请求参数/请求体传入）：

```http
POST http://localhost:8081/api/orders
Content-Type: application/json

{
  "userId": 10001,
  "orderType": "TAKEOUT",
  "campusId": 1,
  "storeId": 1,
  "addressId": 1,
  "deliveryFeeCent": 300,
  "remark": "Send to dorm",
  "items": [
    {
      "skuId": 101,
      "title": "Spicy Chicken Hotpot Large",
      "unitPriceCent": 2999,
      "quantity": 1
    }
  ]
}
```

返回字段包含 `id / status / totalAmountCent / expireAt`，其中 `totalAmountCent` 会由服务端按 `items + deliveryFeeCent` 汇总计算。

#### 4.3 支付（模拟）
```http
POST http://localhost:8081/api/orders/{id}/pay?userId=10001
```

#### 4.4 商家确认
```http
POST http://localhost:8081/api/orders/{id}/merchant/confirm?merchantUserId=30001
```

#### 4.5 骑手接单/取餐/开始配送/完成
```http
POST http://localhost:8081/api/orders/{id}/rider/take?riderUserId=20001
POST http://localhost:8081/api/orders/{id}/rider/pickup?riderUserId=20001
POST http://localhost:8081/api/orders/{id}/complete?userId=10001
```

#### 4.6 订单明细查询
```http
GET http://localhost:8081/api/orders/{id}/items
```

### 5) IDEA 里如何观察日志（推荐）

1. 直接以 `Spring Boot` 方式运行：分别运行 `com.campushelp.order.OrderApplication` 和 `com.campushelp.product.ProductApplication`。
2. 控制台会输出：
   - 每次请求的 `HTTP METHOD URI => status (xx ms)`（`RequestLogFilter`）
   - MyBatis-Plus 的 SQL（`mybatis-plus.configuration.log-impl`）

超时未支付：服务内置定时任务扫描，将 `CREATED` 且过期的订单置为 `CANCELLED`（生产可换 RabbitMQ 延迟队列）。

> 说明：`order-service` 默认会执行 `classpath:db/schema-order.sql` 来创建最小表（`ch_order`）。
> 如果你已手动执行了 `docs/sql/001_campus_help_schema_mysql8.sql`，也可以把 `spring.sql.init.mode` 改为 `never`。

