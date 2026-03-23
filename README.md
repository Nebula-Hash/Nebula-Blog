<div align="center">

# 🌌 Nebula Blog

**现代化全栈博客系统**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5.24-42b883.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

一个基于 Spring Boot 4.0 + Vue 3 的前后端分离博客系统  
提供完整的文章管理、用户互动、权限控制等功能

[功能特性](#-功能特性) • [技术架构](#-技术架构) • [快速开始](#-快速开始) • [项目结构](#-项目结构) • [部署指南](#-部署指南)

</div>

---

## 📖 项目简介

Nebula Blog 是一个现代化的全栈博客系统，采用前后端分离架构设计。系统包含三个独立应用：

- **博客前台**（blog-web）：面向普通用户的博客浏览、评论、互动平台
- **管理后台**（admin-web）：面向管理员的内容管理、数据统计平台
- **后端服务**（Spring-server）：提供统一的 RESTful API 服务

系统采用最新技术栈构建，代码结构清晰，注重安全性和用户体验，适合学习和二次开发。

## ✨ 功能特性

### 博客前台

- 📝 **文章浏览**：支持文章列表、详情、分类、标签筛选
- 🔍 **全文搜索**：快速搜索文章标题和内容
- 💬 **评论互动**：支持文章评论、回复、点赞功能
- 🎨 **Markdown 渲染**：完整的 Markdown 语法支持，代码高亮
- 📊 **数据统计**：浏览量、点赞数、收藏数实时统计
- 🎯 **轮播推荐**：首页轮播图展示热门文章
- 🌓 **主题切换**：支持明暗主题切换
- 📱 **响应式设计**：完美适配桌面端和移动端

### 管理后台

- 📊 **数据面板**：系统数据概览、访问统计、趋势分析
- ✍️ **文章管理**：文章发布、编辑、删除、草稿管理
- 🏷️ **分类标签**：灵活的分类和标签管理
- 👥 **用户管理**：用户列表、状态管理、权限控制
- 💬 **评论管理**：评论审核、删除、批量操作
- 🎨 **轮播图管理**：首页轮播图配置
- 📈 **数据可视化**：ECharts 图表展示数据趋势

### 系统特性

- 🔐 **安全认证**：基于 Sa-Token 的 JWT 认证，支持 Token 无感刷新
- 🛡️ **权限控制**：细粒度的角色权限管理（管理员/普通用户）
- 🚀 **性能优化**：Redis 缓存、数据库连接池、前端懒加载
- 📦 **文件上传**：支持阿里云 OSS 对象存储
- 🔄 **登录保护**：防暴力破解、注册频率限制
- 📝 **操作日志**：关键操作日志记录
- 🐳 **容器化部署**：完整的 Docker 部署方案

## 🛠️ 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.0.1 | 核心框架 |
| Spring WebMvc | - | Web 框架 |
| MyBatis-Plus | 3.5.16 | ORM 框架 |
| Sa-Token | 1.39.0 | 权限认证框架 |
| MySQL | 8.x | 关系型数据库 |
| Redis | - | 缓存/会话存储 |
| HikariCP | - | 数据库连接池 |
| Aliyun OSS | 3.18.4 | 对象存储服务 |
| Hutool | 5.8.25 | Java 工具库 |
| Lombok | 1.18.42 | 代码简化工具 |
| Commonmark | 0.21.0 | Markdown 解析 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.24 | 渐进式前端框架 |
| Vue Router | 4.2.5 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Naive UI | 2.38.0+ | UI 组件库 |
| Axios | 1.6.2 | HTTP 客户端 |
| Vite | 7.2.4 | 构建工具 |
| Markdown-it | 14.0.0 | Markdown 渲染 |
| Highlight.js | 11.9.0 | 代码高亮 |
| ECharts | 6.0.0 | 数据可视化（管理后台） |

### 系统架构

```
┌─────────────────┐      ┌─────────────────┐
│   Blog Web      │      │   Admin Web     │
│  (Vue 3 SPA)    │      │  (Vue 3 SPA)    │
└────────┬────────┘      └────────┬────────┘
         │                        │
         │    HTTP/HTTPS (REST)   │
         └────────┬───────────────┘
                  │
         ┌────────▼────────┐
         │  Spring Boot    │
         │   API Server    │
         │  (Sa-Token JWT) │
         └────────┬────────┘
                  │
         ┌────────┴────────┐
         │                 │
    ┌────▼─────┐    ┌─────▼─────┐
    │  MySQL   │    │   Redis   │
    │ Database │    │   Cache   │
    └──────────┘    └───────────┘
```

## 📁 项目结构

```
nebula-blog/
├── Spring-server/                  # 后端服务（多模块 Maven 项目）
│   ├── Common/                     # 公共模块
│   │   ├── constant/              # 常量定义
│   │   ├── enumeration/           # 枚举类
│   │   ├── exception/             # 异常类
│   │   └── utils/                 # 工具类
│   ├── Data_Object/               # 数据对象模块
│   │   ├── dto/                   # 数据传输对象
│   │   ├── entity/                # 实体类
│   │   └── vo/                    # 视图对象
│   ├── Server/                    # 业务服务模块
│   │   ├── config/                # 配置类
│   │   ├── controller/            # 控制器层
│   │   ├── service/               # 业务逻辑层
│   │   ├── mapper/                # 数据访问层
│   │   ├── aspect/                # AOP 切面
│   │   ├── handler/               # 异常处理器
│   │   ├── interceptor/           # 拦截器
│   │   └── resources/             # 配置文件
│   └── pom.xml                    # Maven 父项目配置
│
├── blog-web/                      # 博客前台
│   ├── src/
│   │   ├── api/                   # API 接口封装
│   │   ├── components/            # 公共组件
│   │   │   ├── article/          # 文章相关组件
│   │   │   ├── comment/          # 评论相关组件
│   │   │   └── common/           # 通用组件
│   │   ├── layout/                # 布局组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # Pinia 状态管理
│   │   ├── utils/                 # 工具函数
│   │   ├── views/                 # 页面组件
│   │   ├── App.vue                # 根组件
│   │   └── main.js                # 入口文件
│   ├── Dockerfile                 # Docker 镜像构建
│   ├── package.json               # 依赖配置
│   └── vite.config.js             # Vite 配置
│
├── admin-web/                     # 管理后台
│   ├── src/
│   │   ├── api/                   # API 接口封装
│   │   ├── components/            # 公共组件
│   │   ├── composables/           # 组合式函数
│   │   ├── config/                # 配置文件
│   │   ├── layout/                # 布局组件
│   │   ├── router/                # 路由配置
│   │   ├── services/              # 业务服务
│   │   │   ├── authService.js    # 认证服务
│   │   │   └── tokenService.js   # Token 管理
│   │   ├── stores/                # Pinia 状态管理
│   │   ├── utils/                 # 工具函数
│   │   │   ├── request.js        # HTTP 请求封装
│   │   │   ├── security.js       # 安全工具
│   │   │   └── validators.js     # 表单验证
│   │   ├── views/                 # 页面组件
│   │   ├── App.vue                # 根组件
│   │   └── main.js                # 入口文件
│   ├── Dockerfile                 # Docker 镜像构建
│   ├── package.json               # 依赖配置
│   └── vite.config.js             # Vite 配置
│
├── SQL/                           # 数据库脚本
│   └── nebula-blog.sql           # 数据库初始化脚本
│
├── docker-deployment/             # Docker 部署配置
│   ├── docker-compose.yml        # Docker Compose 配置
│   └── .env.example              # 环境变量示例
│
└── README.md                      # 项目文档
```

## 🚀 快速开始

### 环境要求

- **JDK**: 25+
- **Maven**: 3.6+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 5.0+

### 本地开发

#### 1. 克隆项目

```bash
git clone https://github.com/your-username/nebula-blog.git
cd nebula-blog
```

#### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE nebula_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入数据库脚本
mysql -u root -p nebula_blog < SQL/nebula-blog.sql
```

#### 3. 配置后端

编辑 `Spring-server/Server/src/main/resources/application-secret-dev.properties`：

```properties
# 数据库配置
spring.datasource.password=your_mysql_password

# Redis 配置
spring.data.redis.password=your_redis_password

# 阿里云 OSS 配置（可选）
upload.oss.endpoint=your_oss_endpoint
upload.oss.access-key-id=your_access_key_id
upload.oss.access-key-secret=your_access_key_secret
upload.oss.bucket-name=your_bucket_name
```

#### 4. 启动后端服务

```bash
cd Spring-server
mvn clean install
cd Server
mvn spring-boot:run
```

后端服务将在 `http://localhost:8081` 启动

#### 5. 启动博客前台

```bash
cd blog-web
npm install
npm run dev
```

博客前台将在 `http://localhost:3000` 启动

#### 6. 启动管理后台

```bash
cd admin-web
npm install
npm run dev
```

管理后台将在 `http://localhost:3001` 启动

### 默认账号

- **管理员账号**: `admin` / `123456`
- **普通用户**: 可通过注册功能创建

## 🐳 部署指南

### Docker Compose 部署（推荐）

#### 1. 构建镜像

```bash
# 构建后端镜像
cd Spring-server
mvn clean package -DskipTests
docker build -t nebula-blog-backend:latest .

# 构建博客前台镜像
cd ../blog-web
npm run build
docker build -t nebula-blog-web:latest .

# 构建管理后台镜像
cd ../admin-web
npm run build
docker build -t nebula-blog-admin:latest .
```

#### 2. 配置环境变量

```bash
cd docker-deployment
cp .env.example .env
# 编辑 .env 文件，配置数据库、Redis、OSS 等信息
```

#### 3. 启动服务

```bash
docker-compose up -d
```

服务访问地址：
- 博客前台: `http://localhost:3000`
- 管理后台: `http://localhost:3001`
- 后端 API: `http://localhost:8081`

#### 4. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f blog-web
docker-compose logs -f admin-web
```

#### 5. 停止服务

```bash
docker-compose down
```

### 生产环境建议

1. **反向代理**: 使用 Nginx 作为反向代理，配置 HTTPS
2. **数据库**: 使用独立的 MySQL 服务器，定期备份
3. **缓存**: 使用独立的 Redis 服务器，配置持久化
4. **文件存储**: 使用阿里云 OSS 或其他对象存储服务
5. **监控**: 配置日志收集和监控告警
6. **安全**: 修改默认密码，配置防火墙规则

## 📝 API 文档

后端服务启动后，可访问以下地址查看 API 文档：

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- API Docs: `http://localhost:8081/v3/api-docs`

主要 API 端点：

### 客户端 API (`/api/client`)

- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `GET /article/list` - 文章列表
- `GET /article/detail/{id}` - 文章详情
- `POST /comment/add` - 添加评论
- `GET /category/list` - 分类列表
- `GET /tag/list` - 标签列表

### 管理端 API (`/api/admin`)

- `POST /auth/login` - 管理员登录
- `GET /article/list` - 文章管理列表
- `POST /article/add` - 发布文章
- `PUT /article/update` - 更新文章
- `DELETE /article/delete/{id}` - 删除文章
- `GET /user/list` - 用户列表
- `GET /data-panel/overview` - 数据概览

## 🔧 配置说明

### 后端配置

主要配置文件位于 `Spring-server/Server/src/main/resources/`：

- `application.properties` - 主配置文件
- `application-dev.properties` - 开发环境配置
- `application-prod.properties` - 生产环境配置
- `application-secret-{profile}.properties` - 敏感信息配置（需自行创建）

### 前端配置

#### 博客前台 (`blog-web/.env.development`)

```env
VITE_API_BASE_URL=http://localhost:8081/api/client
```

#### 管理后台 (`admin-web/.env.development`)

```env
VITE_API_BASE_URL=http://localhost:8081/api/admin
```

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 开源协议

本项目采用 [MIT](LICENSE) 协议开源。

## 👨‍💻 作者

**Nebula-Hash**

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Naive UI](https://www.naiveui.com/)
- [Sa-Token](https://sa-token.cc/)
- [MyBatis-Plus](https://baomidou.com/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！**

</div>
