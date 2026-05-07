# 环境变量配置说明

## 📋 快速开始

### 1️⃣ 创建 .env 文件
```bash
# 在项目根目录复制示例文件
cp .env.example .env
```

### 2️⃣ 编辑 .env 文件
根据你的本地环境修改配置值，例如：
- 数据库密码
- Redis 密码（如果有）
- RabbitMQ 账号密码
- JWT 密钥（生产环境必须修改）

### 3️⃣ 启动应用
Spring Boot 会自动加载 `.env` 文件中的变量，无需额外配置。

---

## 🔒 安全说明

- ✅ `.env` 文件已在 `.gitignore` 中，**不会被提交到 Git**
- ✅ `.env.example` 是模板文件，包含默认值，可以提交
- ⚠️ **切勿**将真实的敏感信息（密码、密钥）提交到版本控制

---

## 📝 配置项说明

### 必需配置（本地开发）
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_PASSWORD | MySQL 密码 | 233615 |
| REDIS_HOST | Redis 地址 | 127.0.0.1 |
| SPRING_RABBITMQ_HOST | RabbitMQ 地址 | 127.0.0.1 |

### 可选配置
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| OSS_* | 阿里云 OSS 配置 | 禁用 |
| CAMPUS_SECKILL_ENABLED | 秒杀功能 | false |
| CAMPUS_MQ_ORDER_DELAY_ENABLED | 延迟订单 | false |

---

## 🌍 不同环境的配置

### 开发环境（dev）
直接修改 `.env` 文件即可。

### 生产环境（prod）
推荐使用系统环境变量或容器编排工具（如 Docker）注入：
```bash
# Linux/Mac
export DB_PASSWORD=your_secure_password

# Windows PowerShell
$env:DB_PASSWORD="your_secure_password"

# Docker
docker run -e DB_PASSWORD=your_secure_password ...
```

---

## ❓ 常见问题

**Q: 修改 .env 后需要重启应用吗？**  
A: 是的，环境变量在应用启动时加载，修改后需要重启。

**Q: 为什么我的配置没有生效？**  
A: 检查以下几点：
1. 文件名必须是 `.env`（不是 `.env.txt`）
2. 文件位置必须在项目根目录
3. 格式必须是 `KEY=VALUE`（不要有空格）
4. 查看启动日志确认 spring-dotenv 已加载

**Q: 可以同时使用 .env 和系统环境变量吗？**  
A: 可以，优先级为：系统环境变量 > .env 文件 > yml 默认值
