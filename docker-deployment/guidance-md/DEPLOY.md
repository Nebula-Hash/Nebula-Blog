# Nebula Blog Docker 部署指南

本指南对应当前仓库的容器化部署方案：

- 本地构建三端镜像
- 将镜像导出后上传到 Ubuntu 22.04.5 生产服务器
- 在服务器中导入镜像并使用 `docker compose` 启动
- 由宝塔 Nginx 统一处理域名、SSL 和反向代理

## 1. 交付文件

- `Spring-server/Dockerfile`
- `blog-web/Dockerfile`
- `admin-web/Dockerfile`
- `docker-deployment/docker-compose.yml`
- `docker-deployment/.env.example`
- `docker-deployment/baota-blog-web.conf`
- `docker-deployment/baota-blog-admin.conf`
- `docker-deployment/baota-blog-server.conf`

## 2. 本地构建镜像

在项目根目录执行：

```powershell
cd Spring-server
mvn -pl Server -am -DskipTests package
cd ..

cd blog-web
npm.cmd run build
cd ..

cd admin-web
npm.cmd run build
cd ..

docker build -t nebula-blog-backend:latest -f Spring-server/Dockerfile Spring-server
docker build -t nebula-blog-web:latest -f blog-web/Dockerfile blog-web
docker build -t nebula-blog-admin:latest -f admin-web/Dockerfile admin-web
```

构建完成后检查：

```powershell
docker images | Select-String "nebula-blog"
```

## 3. 导出镜像

建议在项目根目录创建临时输出目录：

```powershell
New-Item -ItemType Directory -Force docker-deployment/out | Out-Null
docker save -o docker-deployment/out/nebula-blog-backend.tar nebula-blog-backend:latest
docker save -o docker-deployment/out/nebula-blog-web.tar nebula-blog-web:latest
docker save -o docker-deployment/out/nebula-blog-admin.tar nebula-blog-admin:latest
```

## 4. 上传到服务器

将以下内容上传到服务器同一目录，例如 `/opt/nebula-blog`：

- `docker-deployment/docker-compose.yml`
- `docker-deployment/.env.example` 复制后改名为 `.env`
- `docker-deployment/out/*.tar`

如果需要，也可一并上传：

- `docker-deployment/baota-blog-web.conf`
- `docker-deployment/baota-blog-admin.conf`
- `docker-deployment/baota-blog-server.conf`

## 5. 服务器准备

登录 Ubuntu 服务器后：

```bash
mkdir -p /opt/nebula-blog
cd /opt/nebula-blog
mkdir -p volumes/backend-logs
```

把 `.env.example` 改为 `.env`：

```bash
cp .env.example .env
```

然后编辑 `.env`，填入真实的：

- MySQL 用户名 / 密码
- Redis 密码
- OSS 配置

注意：

- MySQL / Redis 访问路径固定使用 `host.docker.internal`
- Compose 已通过 `host-gateway` 把该域名映射到宿主机

## 6. 服务器导入镜像

```bash
docker load -i nebula-blog-backend.tar
docker load -i nebula-blog-web.tar
docker load -i nebula-blog-admin.tar
```

检查：

```bash
docker images | grep nebula-blog
```

## 7. 启动容器

```bash
docker compose up -d
docker compose ps
```

查看日志：

```bash
docker compose logs -f backend
docker compose logs -f blog-web
docker compose logs -f admin-web
```

## 8. 健康检查与服务验证

在服务器本机验证：

```bash
curl http://127.0.0.1:8081/actuator/health
curl http://127.0.0.1:8081/actuator/health/readiness
curl http://127.0.0.1:3000/healthz
curl http://127.0.0.1:3001/healthz
```

如果后端连接 MySQL / Redis 正常，`readiness` 应返回 `UP`。

## 9. 宝塔 Nginx 配置

三个模板文件分别对应：

- `baota-blog-web.conf`
- `baota-blog-admin.conf`
- `baota-blog-server.conf`

关键要求：

- `blog-web.nebula-hash.com`
  - `/` -> `127.0.0.1:3000`
  - `/api/` -> `127.0.0.1:8081`
- `blog-admin.nebula-hash.com`
  - `/` -> `127.0.0.1:3001`
  - `/api/` -> `127.0.0.1:8081`
- `blog-server.nebula-hash.com`
  - `/` -> `127.0.0.1:8081`

额外注意：

- 宝塔反向代理站点要允许至少 `20m` 的请求体，否则封面图、头像、文章图片上传会失败

## 10. 常用运维命令

```bash
docker compose up -d
docker compose down
docker compose restart backend
docker compose restart blog-web
docker compose restart admin-web
docker compose logs --tail=200 backend
docker compose ps
```

## 11. 更新版本

1. 本地重新构建镜像
2. 重新导出 tar
3. 上传到服务器
4. 在服务器执行：

```bash
docker load -i nebula-blog-backend.tar
docker load -i nebula-blog-web.tar
docker load -i nebula-blog-admin.tar
docker compose up -d
```
