# Docker 容器化部署规范 - 需求文档

## 1. 目标

为 Nebula Blog 全栈项目制定一套可直接落地的生产部署方案，满足以下目标：

- 使用 Docker 容器化部署三个应用服务：
  - `Spring-server` 后端服务
  - `blog-web` 用户端前端
  - `admin-web` 管理端前端
- 保持 MySQL、Redis 继续由云主机 / 宝塔面板管理，不容器化
- 通过宝塔 Nginx 统一接入域名、SSL 和反向代理
- 使部署方案与当前代码实现保持一致，避免“文档可行、代码实际不通”的情况

## 2. 当前项目事实

以下内容已根据仓库现状确认：

- 后端目录为 `Spring-server`，是 Maven 多模块工程，实际可执行服务模块为 `Spring-server/Server`
- 后端生产端口为 `8081`，开发端口为 `8080`
- 后端健康检查已迁移为 Spring Boot Actuator
- 当前标准健康检查端点为：
  - `GET /actuator/health`
  - `GET /actuator/health/liveness`
  - `GET /actuator/health/readiness`
- 前端两个项目均为 Vue 3 + Vite，生产构建后为纯静态资源
- 前端容器运行时提供静态页面服务，并支持同源 `/api/` 转发到后端容器
- 两个前端线上请求使用固定相对路径：
  - 用户端：`/api/client`
  - 管理端：`/api/admin`
- `VITE_API_BASE_URL` 当前只参与 Vite 开发代理，不会在浏览器生产运行时决定 Axios 基础地址
- 后端生产环境已启用文件日志，日志目录配置项为 `logging.file.path`
- `application-secret-dev.properties` 与 `application-secret-prod.properties` 已存在于本地工作区，但未被 Git 跟踪，符合“不提交敏感信息”的方向

## 3. 部署范围

### 3.1 本次部署必须覆盖

- [x] 后端服务容器化
- [x] 用户端前端容器化
- [x] 管理端前端容器化
- [x] Docker Compose 编排
- [x] 宝塔 Nginx 域名与 SSL 配置
- [x] 后端日志持久化
- [x] 敏感配置外置

### 3.2 本次部署明确不做

- [x] MySQL 容器化
- [x] Redis 容器化
- [x] 首轮即接入完整监控告警平台
- [x] 首轮即做 K8s / Swarm 编排

## 4. 功能需求

### 4.1 镜像与容器

- 后端镜像支持多阶段构建
- 前端镜像支持多阶段构建
- 在网络受限环境下，允许先在宿主机构建后端 jar 与前端 dist，再由 Docker 镜像封装运行时
- 所有镜像应具备明确的构建上下文和最小必要内容
- 所有容器应支持 `restart: unless-stopped` 或等效自动拉起策略

### 4.2 配置管理

- 后端必须支持通过环境变量覆盖以下生产配置：
  - `SPRING_PROFILES_ACTIVE`
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_DATA_REDIS_HOST`
  - `SPRING_DATA_REDIS_PORT`
  - `SPRING_DATA_REDIS_PASSWORD`
  - `SPRING_DATA_REDIS_DATABASE`
  - `UPLOAD_OSS_ENDPOINT`
  - `UPLOAD_OSS_ACCESS_KEY_ID`
  - `UPLOAD_OSS_ACCESS_KEY_SECRET`
  - `UPLOAD_OSS_BUCKET_NAME`
  - `UPLOAD_OSS_CUSTOM_DOMAIN`
  - `LOGGING_FILE_PATH`
- 敏感信息不得写入镜像，不得提交到仓库
- 允许使用两种外置方式，二选一：
  - 环境变量注入
  - 外部配置文件挂载

### 4.3 路由与代理

当前代码要求生产环境具备以下反向代理能力：

- `blog-web.nebula-hash.com`
  - `/` -> 用户端前端容器
  - `/api/` -> 后端容器
- `blog-admin.nebula-hash.com`
  - `/` -> 管理端前端容器
  - `/api/` -> 后端容器
- `blog-server.nebula-hash.com`
  - `/` -> 后端容器

说明：

- 如果不在前端域名上代理 `/api/`，当前前端生产代码将无法正常访问后端
- 因此前端“API 地址改为生产绝对域名”不是当前部署前提，反向代理才是当前代码对应方案

### 4.4 前端静态站点要求

- 两个前端容器都必须支持 SPA history 路由刷新
- 前端容器负责静态资源服务，并支持同源 `/api/` 转发
- 静态资源应启用合理缓存与 gzip

### 4.5 健康检查

- 使用 Spring Boot Actuator 作为唯一健康检查方案
- 容器健康检查默认使用 `GET /actuator/health`
- 可按需使用 `GET /actuator/health/liveness` 与 `GET /actuator/health/readiness`
- `readiness` 应明确纳入 `db` 与 `redis`，避免只反映应用可用性状态而不反映外部依赖状态
- 原有自定义 `/ping`、`/health` 方案不再保留

## 5. 非功能需求

### 5.1 可靠性

- 支持单服务独立重启
- 容器异常退出后自动拉起
- 后端日志可在宿主机持续查看

### 5.2 安全性

- 不将敏感配置打包进镜像
- 宿主机仅暴露必要端口
- Nginx 统一处理 HTTPS

### 5.3 可维护性

- 提供部署说明
- 提供环境变量模板
- 提供常用运维命令

## 6. 技术约束

### 6.1 运行环境

- 云主机操作系统：Linux
- 当前已确认生产服务器为 Ubuntu 22.04.5 LTS，2 核 2 GB
- Docker 版本：20.10+
- Docker Compose / `docker compose`：2.x
- Java：25
- Node.js：18+

### 6.2 外部依赖

- MySQL、Redis 位于宿主机或宿主机可访问地址
- Docker 容器访问宿主机数据库 / Redis 采用 `host.docker.internal + host-gateway`
- Compose 中必须显式补充 `extra_hosts: ["host.docker.internal:host-gateway"]`

### 6.3 目录与构建约束

- 后端 Docker 构建上下文应以 `Spring-server` 根目录为准，而不是错误引用不存在的 `SpringBoot`
- 后端打包需兼容 Maven 多模块结构
- 前端构建应优先使用现有 `package-lock.json`

## 7. 验收标准

### 7.1 构建验收

- [ ] 能成功构建后端镜像
- [ ] 能成功构建 `blog-web` 镜像
- [ ] 能成功构建 `admin-web` 镜像

### 7.2 部署验收

- [ ] `docker compose up -d` 后三个容器均正常启动
- [ ] 后端可连通 MySQL 与 Redis
- [ ] 宝塔 Nginx 代理配置生效

### 7.3 业务验收

- [ ] `https://blog-web.nebula-hash.com` 可访问
- [ ] `https://blog-admin.nebula-hash.com` 可访问
- [ ] `https://blog-server.nebula-hash.com/actuator/health` 可访问
- [ ] 用户端核心浏览功能可用
- [ ] 管理端登录与核心管理功能可用
- [ ] OSS 上传功能可用

### 7.4 运维验收

- [ ] 日志已挂载到宿主机
- [ ] 能单独重启任一服务
- [ ] 能查看容器状态与日志

## 8. 风险清单

- Java 25 基础镜像选择需要与实施时镜像仓库实际可用 tag 对齐
- Ubuntu 上 `host.docker.internal` 依赖 `host-gateway` 映射，Compose 配置遗漏会直接导致后端无法连通 MySQL / Redis
- 当前前端生产 API 路径依赖反向代理，若 Nginx 规则遗漏会直接导致前端接口 404
- 测试 SSL 证书有效期较短，需明确续期方式

## 9. 本阶段仍需补充的信息

- 首轮部署建议采用“本地构建镜像后上传 / 导入服务器”，避免在 2 核 2 GB 服务器上执行三端构建
- `blog-server.nebula-hash.com` 是否需要对公网开放 Swagger / API 文档
