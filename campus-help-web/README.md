# campus-help-web（Vite + React）

前后端分离前端：TypeScript、Tailwind CSS v4、TanStack Query、React Router、Zustand（持久化 Token）。

后端为 **单体** `campus-help-server`（默认 **http://localhost:8080**），所有接口前缀 **`/api/v3`**。

## 环境变量

复制 `.env.example` 为 `.env.development`（可选）：

| 变量 | 说明 |
|------|------|
| `VITE_API_BASE` | 后端根地址。**留空** 时使用相对路径 `/api/...`，由 Vite 开发服务器 **proxy** 转发到 `http://localhost:8080`（见 `vite.config.ts`）。生产构建若静态资源与 API 不同域，设为完整 API 根 URL。 |

旧版多端口变量（`VITE_USER_API` 等）已废弃。

后端需配置 **CORS**（`campus.cors.allowed-origins`，默认含 `http://localhost:5173`）。

## 启动

```bash
cd campus-help-web
npm install
npm run dev
```

浏览器：`http://localhost:5173`

## 联调顺序

1. MySQL 执行 `docs/sql`：**`001` → `002` → `005`**（见仓库 `docs/sql/README.md`）。
2. 启动后端：`mvn -pl campus-help-server -am spring-boot:run`（仓库根目录 `campus-help`）。
3. 使用种子账号登录，例如学生 **`13800010001` / `123456`**，商家 **`13700030001`**，骑手 **`13900020001`**（详见 `005` 脚本）。

## 脚本

- `npm run dev` — 开发
- `npm run build` — 生产构建
- `npm run preview` — 预览构建结果
