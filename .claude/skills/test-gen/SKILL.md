---
name: test-gen
description: 为 Spring Boot 项目生成单元测试（JUnit 5 + Mockito）。当用户要求写测试、补测试时调用。
---

# Test Generation Skill

为 Spring Boot 微服务生成 JUnit 5 + Mockito 单元测试。

## 生成的测试种类

### Service 层测试
- 使用 `@ExtendWith(MockitoExtension.class)`
- Mock 所有注入的依赖（Mapper、FeignClient、其他 Service）
- 测试正常流程、异常流程、边界条件
- 使用 `assertThrows` 测试异常

### Controller 层测试
- 使用 `@WebMvcTest`
- Mock Service 层
- `MockMvc` 测试 REST 接口
- 验证响应状态码、响应体

### Mapper 层测试（可选）
- 使用 `@MybatisPlusTest` 或 `@DataJpaTest`
- 内嵌 H2 数据库

## 命名规范

```
OrderServiceTest.java        — OrderService 的测试
OrderControllerTest.java     — OrderController 的测试
SeckillTicketServiceTest.java — SeckillTicketService 的测试
```

## 测试方法命名

```
should_returnSuccess_when_createOrder()      — 正常流程
should_throwException_when_stockNotEnough()  — 异常流程
should_rollback_when_paymentFailed()         — 回滚场景
```

## 覆盖要求
- 每个 public 方法至少一个正常用例、一个异常用例
- 分支语句（if/else、switch）每个分支至少一个用例
- 边界条件（空集合、null 参数、超长字符串）
