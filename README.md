# 图灵学社官网 · 后端服务（tuling）

图灵学社（Turing Society）社团官网的后端服务：Spring Boot 提供 REST API，负责社团内容（活动 / 博客 / 问答 / 资源 / 成员 / 教师 / 项目）的存储与查询、用户认证、AI 流式问答（RAG）以及**活动报名**功能。

| 仓库 | 地址 |
|---|---|
| 后端（本仓库） | https://github.com/luo683/tulingbaoming-houduan |
| 前端 | https://github.com/luo683/tulingbaoming-qianduan |

---

## 技术栈

| 层 | 选型 |
|---|---|
| 运行时 | Java 8 |
| 框架 | Spring Boot 2.7.18（Web + Data JPA） |
| 数据库 | H2 文件库（默认，零安装）／ MySQL 8（可选，配置注释保留） |
| AI | LangChain4j 0.32.0 + 硅基流动 SiliconFlow（Qwen2.5-7B-Instruct 对话、BAAI/bge-m3 向量） |
| 认证 | BCrypt（`spring-security-crypto`）+ 服务端会话式登录 |
| 缓存 / 限流 | 进程内 `LocalCache`（不再依赖 Redis 服务） |
| 其他 | Lombok、Jackson、Bean Validation |

---

## 目录结构

```
src/main/java/com/tuling/
├── TulinApplication.java          # 启动入口
├── config/                        # CORS、数据初始化、种子数据、本地缓存、安全
├── controller/                    # 12 个 REST 控制器（见下方接口清单）
├── dto/                           # 报名请求 / 报名统计等
├── entity/                        # JPA 实体（含 EventRegistration）
├── model/                         # 请求响应模型（登录、聊天等）
├── repository/                    # Spring Data JPA 仓库
└── service/                       # 业务逻辑（含 EventRegistrationService）
src/main/resources/
├── application.yml                # 全部配置
├── db/migration/V2__...sql        # MySQL 版报名表迁移脚本（参考用，见下方说明）
├── knowledge/tuling-knowledge.txt # RAG 知识库语料
└── schema.sql.mysql.bak / seed.sql.mysql.bak  # MySQL 方言建表与种子数据（备用）
```

---

## 环境要求

- JDK 8+
- Maven 3.6+（仓库内 `install-maven.ps1` / `Maven安装指南.md` 是 Windows 离线安装脚本）
- Node.js 18+（仅前端需要）

---

## 快速开始

### 1. 配置 AI Key（必做）

AI 问答 / RAG 检索依赖 SiliconFlow 的 Key，仓库中**不保存真实 Key**，启动前注入环境变量：

```powershell
# PowerShell（当前窗口有效）
$env:SILICONFLOW_API_KEY = "sk-你的key"
```

```bash
# Bash / macOS / Linux
export SILICONFLOW_API_KEY="sk-你的key"
```

未设置也能启动，但 `/api/chat/**` 与 `/api/knowledge/search` 会因鉴权失败返回错误，其余接口不受影响。

### 2. 启动后端

```bash
mvn spring-boot:run
# 或先打包再跑
mvn -DskipTests package && java -jar target/tulin-1.0.0-SNAPSHOT.jar
```

Windows 也可双击 `_run_backend.cmd`。

- 服务地址：http://localhost:8080
- 健康检查：`GET /api/auth/health`、`GET /api/chat/health`
- H2 控制台：http://localhost:8080/h2-console
  - JDBC URL：`jdbc:h2:file:./data/turing`，用户名 `sa`，密码留空

首次启动会自动完成：JPA 建表（`ddl-auto: update`）→ 初始化预置账号 → 写入种子数据（活动 / 问答 / 资源 / 成员 / 教师 / 项目，均为"表空才灌"的幂等逻辑）。

预置账号：

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `tuling2024` | admin |
| `demo` | `demo` | user |

### 3. 启动前端

```bash
cd tuling-web
npm install
npm run dev      # http://localhost:5173
npm run build    # 产物在 .vitepress/dist/
```

前端 API 基址的解析优先级（见前端 `.vitepress/theme/api/config.ts`）：

1. 环境变量 `VITE_API_BASE`（部署到别的机器时用它，如 `VITE_API_BASE=http://10.0.0.5:8080`）
2. 浏览器当前 `hostname` + 后端端口 `8080`（本地 → localhost，局域网 → 192.168.x.x，域名反代 → 同名 host）
3. 构建期（SSR 无 window）回退 `http://localhost:8080`

---

## 环境变量清单

| 变量 | 作用 | 是否必填 | 默认值 |
|---|---|---|---|
| `SILICONFLOW_API_KEY` | SiliconFlow 对话 + Embedding 的 Key，同时注入 `ai.model.api-key` 与 `ai.embedding.api-key` | AI 功能必填 | 空（占位 `${SILICONFLOW_API_KEY:}`） |
| `VITE_API_BASE` | **前端**使用的后端基址覆盖 | 否 | 按 hostname + 8080 推导 |
| `REDIS_HOST` / `REDIS_PORT` | 仅当启用多实例共享缓存、恢复 Redis 依赖时使用（`application.yml` 中默认注释） | 否 | `localhost` / `6379` |

固定端口：后端 `8080`、前端 `5173`，需改动时直接修改 `application.yml` 的 `server.port` 与前端启动参数。

---

## 活动报名功能

### 接口一览

统一前缀 `/api`，响应统一为 `{ "ok": true, "data": ... }` 或 `{ "ok": false, "error": "..." }`。

| 方法 | 路径 | 说明 | 参数 | 成功 | 失败 |
|---|---|---|---|---|---|
| GET | `/api/events/{eventId}/registrations/stats` | 报名统计 | — | 200 `data: RegistrationStats` | 404 活动不存在 |
| POST | `/api/events/{eventId}/registrations` | 报名活动 | query `userId`，body `{realName, phone, email?, notes?}` | 200 `data: Registration` | 404 活动/用户不存在；400 不允许报名、已截止、已报名、人数已满 |
| DELETE | `/api/events/{eventId}/registrations` | 取消报名（置为 `cancelled`） | query `userId` | 200 `message` | 404 无报名记录；400 已取消过、已参加 |
| GET | `/api/registrations/my` | 我的报名记录 | query `userId` | 200 `data: Registration[]` | — |
| GET | `/api/events/{eventId}/registrations/check` | 是否已报名 | query `userId` | 200 `isRegistered: boolean` | — |
| GET | `/api/events/{eventId}/registrations/admin` | 管理员：活动报名名单 | — | 200 `data: Registration[]` | — |
| PUT | `/api/registrations/{id}/attend` | 管理员：标记签到 | — | 200 `data: Registration` | 404 / 400 |
| POST | `/api/registrations/{id}/certificate` | 管理员：写入参训证明链接 | body `{certificateUrl}` | 200 `data: Registration` | 404 / 400 |

### 报名校验顺序（`EventRegistrationService.registerForEvent`）

1. 活动存在 → 2. 用户存在 → 3. `allowRegistration == true` → 4. 未过 `registrationDeadline` → 5. 该用户未存在有效报名 → 6. 未超 `maxParticipants`（`0` 或 `null` 视为不限）→ 7. 落库，状态 `registered`

### 请求示例

```bash
# 报名
curl -X POST "http://localhost:8080/api/events/1/registrations?userId=2" \
  -H "Content-Type: application/json" \
  -d '{"realName":"张三","phone":"13800138000","email":"z@example.com","notes":"想坐前排"}'

# 统计
curl "http://localhost:8080/api/events/1/registrations/stats"
```

### 状态流转

```
registered ──(取消报名)──> cancelled
     │
     └──(管理员签到 PUT /attend)──> attended   # attended 不可再取消
```

### 数据模型

`event_registrations`（实体 `EventRegistration`）：

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | 主键 |
| `event_id` / `user_id` | BIGINT | 活动 / 用户，联合唯一（同一人不能重复报名同一活动） |
| `username` / `student_id` | VARCHAR | 报名时冗余的用户名与学号 |
| `real_name` / `phone` / `email` / `notes` | VARCHAR / TEXT | 报名表单信息 |
| `status` | VARCHAR(20) | `registered` / `attended` / `cancelled` |
| `attended_at` | DATETIME | 签到时间 |
| `certificate_generated` / `certificate_url` | BOOLEAN / VARCHAR | 证明生成标记与链接 |
| `created_at` / `updated_at` | DATETIME | 报名时间 / 更新时间 |

`events` 表为支持报名新增三列：`max_participants`（0 = 不限）、`registration_deadline`、`allow_registration`。

> **关于 `db/migration/V2__add_event_registration_tables.sql`**：这是 MySQL 方言的迁移脚本（含统计视图 `v_event_registration_stats`），**项目未引入 Flyway**，启动不会自动执行。默认 H2 模式下由 JPA `ddl-auto: update` 自动建表；切 MySQL 时手工执行该脚本即可。

---

## 其他接口模块

| 模块 | 前缀 | 主要端点 |
|---|---|---|
| 认证 | `/api/auth` | `POST /register`、`POST /login`、`POST /change-password`、`GET /health` |
| 活动 | `/api/events` | `GET /`、`GET /{id}`、管理员 `POST /admin`、`PUT /admin/{id}`、`DELETE /admin/{id}` |
| 博客 | `/api/blog` | `GET /posts`、`GET /posts/{slug}`、`POST /posts`、评论、审核状态管理 |
| 问答 | `/api/faqs` | `GET /questions`、`POST /ask`、`POST /questions/{id}/reply`、管理员维护 |
| 知识库 | `/api/knowledge` | `GET /search`、`GET /admin/documents`、`POST /admin/documents`、`POST /admin/reindex` |
| 资源 | `/api/resources` | `GET /`、`POST /{id}/like`、`DELETE /{id}/like` |
| 成员 / 教师 / 项目 | `/api/members`、`/api/teachers`、`/api/projects` | 列表 + 管理员 CRUD |
| 搜索 | `/api/search` | 全站聚合搜索 |
| AI 对话 | `/api/chat` | `POST /stream`（SSE 流式）、`POST /clear/{sessionId}`、`GET /rate-limit/status` |

---

## 权限约定

| 动作 | 游客 | 登录用户 | 管理员 |
|---|---|---|---|
| 浏览页面、活动与资源列表 | ✅ | ✅ | ✅ |
| 博客全文 / FAQ 展开 / 下载链接 / 项目链接 | ❌（锁层） | ✅ | ✅ |
| 活动报名、取消报名 | ❌ | ✅ | ✅ |
| 发帖、提问、上传 | ❌（弹登录框） | ✅ | ✅ |
| 管理员接口（`/admin/**`、报名名单与签到） | ❌ | ❌ | ✅ |

> 当前报名接口通过 `userId` 查询参数识别用户（尚未接入 JWT/会话校验），`/admin/**` 接口也仅靠前端角色判断拦截。**生产部署前需要补服务端鉴权**，否则可被直接调用。

---

## 常见问题

| 现象 | 原因 / 处理 |
|---|---|
| `Port 8080 was already in use` | 先 `netstat -ano | findstr :8080` 找 PID，结束旧进程；或改 `server.port` |
| 前端请求全红 / CORS 报错 | 后端需运行在 8080；跨域已由 `CorsConfig` 放开 `/api/**`（`allowedOriginPatterns("*")`） |
| 前端 fetch 超时 | 后端未启动，或前端访问的 host 与后端不在同一网络；用 `VITE_API_BASE` 显式指定 |
| AI 对话返回鉴权错误 | 未设置 `SILICONFLOW_API_KEY` |
| 想重置演示数据 | 停服务后删除 `data/turing.mv.db`，重启即重新建表并灌种子数据 |
| 想切 MySQL | 修改 `spring.datasource` 为 MySQL 连接、驱动改 `com.mysql.cj.jdbc.Driver`，执行 `db/migration/V2__...sql` 与 `schema.sql.mysql.bak`；`switch-to-h2.ps1` 记录了两边配置的差异 |
| 中文注释乱码 | 文件均为 UTF-8，用 UTF-8 编码打开；控制台需 `chcp 65001` |

---

## 安全说明

- 仓库**不放真实 API Key**：`application.yml` 与 `switch-to-h2.ps1` 中均为 `${SILICONFLOW_API_KEY:}` 占位；历史提交同样已脱敏。
- 数据库密码、演示账号口令仅用于本地开发，切勿沿用到生产环境。
- `data/`（H2 运行时库文件）与 `*.log`、`target/` 已列入 `.gitignore`，不入库。

---

## 相关文档

项目根目录另有这些说明文档：

- `后端启动指南.md`、`快速启动方案-H2数据库.md` — 启动与数据库切换
- `后端启动问题诊断与解决.md`、`常见错误解决方案.md` — 排错
- `MySQL完整安装指南.md`、`Maven安装指南.md` — 环境安装
