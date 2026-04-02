## V1（外卖闭环） | MySQL 启动运行手册

适用对象：你希望使用本机 `MySQL`（不走 H2）启动，并在 IDEA/控制台观察“请求访问 + SQL 执行”日志。

---

### 0) 前置条件

1. 本机 MySQL 可访问：默认 `localhost:3306`
2. 账号密码（默认）：`root/root`
3. 端口：`8081`（order-service）/ `8082`（product-service）空闲

---

### 1) 启动（终端方式）

在项目根目录 `D:/work/MYproject/campus-help` 分别开两个终端：

#### 1.1 product-service（8082）

```bash
mvn -f campus-help-product/pom.xml spring-boot:run -DskipTests
```

#### 1.2 order-service（8081）

```bash
mvn -f campus-help-order/pom.xml spring-boot:run -DskipTests
```

> 如果你的 MySQL 不是默认配置，可在启动命令行临时覆盖：
>
> - `DB_URL`
> - `DB_USERNAME`
> - `DB_PASSWORD`

---

### 2) IDEA 启动方式（推荐）

1. 打开 `campus-help/campus-help-order`  
2. 运行主类：`com.campushelp.order.OrderApplication`
3. 打开 `campus-help/campus-help-product`  
4. 运行主类：`com.campushelp.product.ProductApplication`

在 Run Configuration 中你可以添加环境变量（可选）：
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

---

### 3) 如何观察请求与 SQL 日志

你会在控制台看到两类关键信息：

1. **请求日志**（每次请求一行）
   - 来自 `RequestLogFilter`
   - 格式：`HTTP METHOD URI => status (xx ms)`
2. **SQL 日志**
   - 来自 MyBatis-Plus：`mybatis-plus.configuration.log-impl=StdOutImpl`
   - 你可以用它确认每个接口调用是否真的访问了数据库

---

### 4) 验证一条 V1 外卖链路

按 `README.md` 的 `4) V1 外卖闭环（演示）` 从下列顺序访问即可：
1. `product-service` 查 stores/products/skus（8082）
2. `order-service` `POST /api/orders` 下单（8081，传 `items`）
3. `order-service` `/pay` 支付
4. `/merchant/confirm`
5. `/rider/take`、`/rider/pickup`
6. `/complete`
7. 查明细 `/api/orders/{id}/items`

