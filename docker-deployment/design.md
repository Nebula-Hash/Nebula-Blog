# Docker 容器化部署规范 - 设计文档

## 1. 部署架构

### 1.1 推荐架构

```text
Internet
  |
  v
Baota Nginx (宿主机)
  |- blog-web.nebula-hash.com
  |   |- /      -> blog-web container:80
  |   `- /api/  -> backend container:8081
  |
  |- blog-admin.nebula-hash.com
  |   |- /      -> admin-web container:80
  |   `- /api/  -> backend container:8081
  |
  `- blog-server.nebula-hash.com
      `- /      -> backend container:8081

Docker bridge network
  |- blog-web
  |- admin-web
  `- backend

Host services
  |- MySQL (宝塔管理)
  `- Redis (宝塔管理)
```

### 1.2 设计结论

- 宝塔 Nginx 作为唯一公网入口
- 三个应用容器均使用 bridge 网络
- MySQL / Redis 保持宿主机部署
- 前端域名下必须代理 `/api/` 到后端，否则当前前端生产代码无法工作

## 2. 后端容器设计

### 2.1 构建上下文

- 构建根目录：`Spring-server`
- 原因：这是 Maven 多模块工程，`Server` 依赖 `Common` 与 `Data_Object`

### 2.2 镜像设计原则

- 采用多阶段构建
- 构建阶段使用支持 Java 25 的 Maven + JDK 镜像
- 运行阶段使用支持 Java 25 的精简 JRE / JDK 镜像
- 只复制最终运行所需 jar 与必要目录

### 2.3 启动配置

后端容器至少需要以下配置：

- `SPRING_PROFILES_ACTIVE=prod`
- 数据库连接信息
- Redis 连接信息
- OSS 连接信息
- `LOGGING_FILE_PATH=/app/logs`

### 2.4 日志设计

当前代码生产日志默认写入 `logging.file.path` 指定目录，建议：

- 容器内路径：`/app/logs`
- 宿主机挂载：`./volumes/backend-logs:/app/logs`

### 2.5 健康检查设计

当前代码状态下建议分两层：

- Docker healthcheck：使用 `/actuator/health`
- 容器编排 / 运维增强：可使用 `/actuator/health/liveness` 与 `/actuator/health/readiness`

说明：

- 原有自定义 `/ping`、`/health` 方案已移除
- 当前采用标准 Spring Boot Actuator 方案，便于 Docker 与后续监控接入

## 3. 前端容器设计

### 3.1 通用设计

- `blog-web` 与 `admin-web` 都采用多阶段构建
- 构建阶段使用 Node.js
- 运行阶段使用 Nginx 提供静态资源

### 3.2 Nginx 容器职责

前端容器内 Nginx 只做两件事：

- 提供静态资源
- 支持 SPA history 路由回退到 `index.html`

不建议在前端容器内再代理后端 API，原因如下：

- 当前生产流量入口已经统一交给宝塔 Nginx
- API 转发在宿主机入口层统一维护更清晰
- 便于同一域名下 `/` 和 `/api/` 的精确路由控制

### 3.3 前端 API 访问设计

当前前端代码不是在浏览器里读取 `VITE_API_BASE_URL`，而是固定访问：

- 用户端：`/api/client`
- 管理端：`/api/admin`

因此生产方案必须选择以下其一：

1. 保持当前代码不动，在宝塔 Nginx 上代理前端域名下的 `/api/`
2. 修改前端代码，使浏览器运行时基于环境变量访问 `https://blog-server.nebula-hash.com`

本次部署设计采用方案 1，因为它与当前代码最一致、改动最小。

## 4. Docker Compose 设计

### 4.1 服务清单

- `backend`
- `blog-web`
- `admin-web`

### 4.2 端口暴露建议

- `backend`: 宿主机 `127.0.0.1:8081:8081`
- `blog-web`: 宿主机 `127.0.0.1:3000:80`
- `admin-web`: 宿主机 `127.0.0.1:3001:80`

说明：

- 端口仅监听本机回环地址，由宝塔 Nginx 转发
- 不建议直接把应用端口暴露到公网

### 4.3 网络访问宿主机服务

Linux 环境下推荐两种做法：

#### 方案 A：显式宿主机地址

- 直接在环境变量中配置 MySQL / Redis 的宿主机实际 IP

#### 方案 B：使用 `host.docker.internal`

- 在 Compose 中补充：
  - `extra_hosts: ["host.docker.internal:host-gateway"]`

说明：

- 本项目已确认采用该方案
- 生产服务器为 Ubuntu 22.04.5 LTS，不能把 Linux 上“直接可用 `host.docker.internal`”当作默认前提

### 4.4 资源限制

本次首轮部署不把 `deploy.resources` 作为核心方案，原因：

- 普通 `docker compose` 场景下，`deploy` 字段常常不会按预期生效

若后续需要资源约束，再根据服务器 Docker / Compose 版本决定是否采用：

- `mem_limit`
- `cpus`
- systemd / cgroup 层限制

## 5. 宝塔 Nginx 设计

### 5.1 blog-web 域名

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location / {
    proxy_pass http://127.0.0.1:3000;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 5.2 blog-admin 域名

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location / {
    proxy_pass http://127.0.0.1:3001;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 5.3 blog-server 域名

```nginx
location / {
    proxy_pass http://127.0.0.1:8081;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 5.4 HTTPS 与安全头

- 由宝塔 Nginx 配置 SSL
- 强制 HTTPS
- 开启 gzip
- 为静态资源添加缓存头
- 可增加基础安全头，但不把它作为首轮部署阻塞项

## 6. 配置注入设计

### 6.1 推荐方式

优先使用 Compose `.env` + `environment` 注入通用配置：

- 数据库 URL / 用户名 / 密码
- Redis host / port / password / database
- OSS 参数
- 日志路径

### 6.2 备选方式

对不适合直接放环境变量的内容，可挂载外部配置文件：

- `application-secret-prod.properties`

### 6.3 变量模板范围

本次部署至少需要整理出以下模板项：

- `COMPOSE_PROJECT_NAME`
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

## 7. 验证设计

### 7.1 最小技术验证

- 本地或服务器上成功构建三个镜像
- Compose 成功启动三个服务
- 宝塔 Nginx 代理规则生效

### 7.2 最小业务验证

- 用户端首页可打开
- 用户端文章详情页刷新不 404
- 管理端登录可用
- `/api/client/*` 与 `/api/admin/*` 请求可正常通达后端
- `/actuator/health` 可访问
- OSS 上传成功

## 8. 结论

本次部署应优先做“最小可上线闭环”：

1. 容器化三个应用
2. 配好宿主机反向代理
3. 外置敏感配置
4. 完成关键业务验证

监控、告警、镜像扫描、资源调优等内容保留为后续增强项，不应阻塞首轮生产部署。
