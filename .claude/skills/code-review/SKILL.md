---
name: code-review
description: Spring Boot / Java 代码审查，覆盖安全、性能、架构、设计模式。当用户要求审查代码、分析代码质量时调用。
---

# Code Review Skill

对 Java / Spring Boot 项目进行结构化代码审查，按以下优先级审查：

## 1. 安全风险（Critical）
- SQL 注入（检查 MyBatis/MyBatis-Plus 的 `$` 拼接）
- JWT 安全（密钥硬编码、过期时间）
- 权限校验遗漏（Controller 是否遗漏 `@RequireRole` 或鉴权）
- API 签名校验缺失
- 敏感信息泄露（日志打印密码/token）

## 2. 性能问题（High）
- N+1 查询（MyBatis-Plus 循环内查询数据库）
- 未使用缓存（高频读取的数据没走 CacheFacade）
- 线程池使用不当（`@Scheduled` 默认单线程阻塞）
- Redis 大 Key / 热点 Key 未处理
- 循环内调用 RPC / MQ

## 3. 并发正确性（High）
- 竞态条件（非原子操作 `check-then-act`）
- 缓存击穿/雪崩防护缺失
- 库存扣减非原子（Redis Lua vs Java 代码扣减）
- 分布式锁使用不当（锁范围过大/过小）

## 4. 架构与设计（Medium）
- 循环依赖（模块间或 Bean 间）
- 事务边界不合理（事务过大或漏加 `@Transactional`）
- 异常处理不当（吞异常、未回滚）
- SPI / 策略模式可扩展性

## 5. 代码质量（Medium/Low）
- 命名不规范
- 魔法数字
- 重复代码
- 未使用的 import / 方法

## 输出格式

每个问题包含：
```
## [严重级别] 问题描述
- 位置: 文件名:行号
- 问题: 具体描述
- 建议: 修复方案 + 代码示例
- 影响范围: 描述可能影响的业务
```
