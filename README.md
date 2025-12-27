
<div align="center">
<h1>Nebula Blog - 现代化博客系统</h1>

一个基于 Spring Boot 4.0 + Vue 3 的前后端分离博客系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0--M2-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.24-42b883.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.14-blue.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

</div>

## 📖 项目简介

Nebula Blog 是一个现代化的博客系统，采用前后端分离架构，提供完整的文章发布、评论互动、用户管理等功能。系统使用最新的技术栈，代码简洁优雅，适合学习和二次开发。

### ✨ 核心特性

- 🎨 **现代化界面** - 基于 Naive UI 构建的简洁美观界面
- 📝 **Markdown 编辑** - 支持 Markdown 格式编写文章
- 🔐 **完善的权限管理** - 基于 Sa-Token 的权限认证体系
- 💬 **评论系统** - 支持文章评论、回复、点赞功能
- 🏷️ **分类标签** - 灵活的分类和标签管理
- 🔍 **全文搜索** - 支持文章标题和内容搜索
- 📊 **数据统计** - 浏览量、点赞数、收藏数等统计
- 📱 **响应式设计** - 适配各种设备屏幕

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 4.0.0-M2 | 核心框架 |
| Spring WebMvc | - | Web 框架 |
| MyBatis-Plus | 3.5.14 | ORM 框架 |
| Sa-Token | 1.39.0 | 权限认证框架 |
| MySQL | 8.x | 数据库 |
| Redis | - | 缓存/会话存储 |
| Druid | 1.2.23 | 数据库连接池 |
| Lombok | - | 代码简化工具 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.24 | 前端框架 |
| Vue Router | 4.2.5 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Naive UI | 2.38.0 | UI 组件库 |
| Axios | 1.6.2 | HTTP 客户端 |
| Vite | 7.2.4 | 构建工具 |
| Markdown-it | 14.0.0 | Markdown 渲染 |
| Highlight.js | 11.9.0 | 代码高亮 |

## 📁 项目结构

```
nebula-blog/
├── blog-web/                    # 前端项目
│   ├── src/
│   │   ├── api/                # API 接口封装
│   │   ├── components/         # 公共组件
│   │   ├── layout/             # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面组件
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── package.json
│   └── vite.config.js          # Vite 配置
├── src/main/
│   ├── java/com/nebula/boot4/
│   │   ├── common/             # 公共类
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器层
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── exception/          # 异常处理
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── service/            # 业务逻辑层
│   │   ├── utils/              # 工具类
│   │   └── vo/                 # 视图对象
│   └── resources/
│       ├── application.properties  # 应用配置
│       └── db.sql              # 数据库脚本
├── doc/
│   └── db.sql                  # 数据库初始化脚本
└── pom.xml                     # Maven 配置
```

## 🚀 快速开始

### 环境要求

- JDK 25
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

1. **创建数据库**

```sql
CREATE DATABASE `boot4-nebula-blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **导入数据库脚本**

执行 `doc/db.sql` 文件初始化数据库表和测试数据

3. **修改配置**

编辑 `src/main/resources/application.properties`，配置数据库连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/boot4-nebula-blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
```

4. **启动 Redis**

```bash
# Windows
redis-server

# Linux/Mac
redis-server
```

5. **启动后端服务**

```bash
# Maven 方式
mvn spring-boot:run

# 或直接运行主类
# Boot4SpringbootProjectApplication.java
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. **安装依赖**

```bash
cd blog-web
npm install
```

2. **启动开发服务器**

```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

3. **构建生产版本**

```bash
npm run build
```

## 👤 测试账号

系统预置了以下测试账号（密码均为：`123456`）：

| 用户名 | 角色 | 说明 |
|--------|------|------|
| admin | 管理员 | 拥有所有权限 |
| author | 作者 | 可以发布文章 |
| user | 普通用户 | 可以评论、点赞 |

## 📚 核心功能

### 用户模块

- ✅ 用户注册/登录
- ✅ 个人信息管理
- ✅ 密码修改
- ✅ 头像上传

### 文章模块

- ✅ 文章发布（支持 Markdown）
- ✅ 文章编辑/删除
- ✅ 文章分类管理
- ✅ 文章标签管理
- ✅ 文章搜索
- ✅ 文章置顶
- ✅ 草稿箱功能
- ✅ 文章历史版本

### 互动模块

- ✅ 文章点赞
- ✅ 文章收藏
- ✅ 文章评论
- ✅ 评论回复
- ✅ 评论点赞
- ✅ 浏览历史

### 展示模块

- ✅ 首页轮播图
- ✅ 热门文章推荐
- ✅ 文章列表（分页）
- ✅ 分类文章列表
- ✅ 标签文章列表

## 🗄️ 数据库设计

### 核心表说明

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| blog_article | 文章表 |
| blog_category | 分类表 |
| blog_tag | 标签表 |
| blog_comment | 评论表 |
| blog_article_like | 文章点赞表 |
| blog_article_collect | 文章收藏表 |
| blog_banner | 轮播图表 |
| blog_view_history | 浏览历史表 |

详细的表结构请查看 `doc/db.sql` 文件

## 🔧 配置说明

### 后端配置

**数据源配置**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/boot4-nebula-blog
spring.datasource.username=root
spring.datasource.password=root
```

**Sa-Token 配置**
```properties
sa-token.token-name=Authorization
sa-token.timeout=2592000
sa-token.is-concurrent=true
```

**MyBatis-Plus 配置**
```properties
mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.global-config.db-config.logic-delete-field=deleted
```

### 前端配置

**API 代理配置**（`vite.config.js`）
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 📝 API 接口

### 认证接口

```
POST   /auth/register      # 用户注册
POST   /auth/login         # 用户登录
POST   /auth/logout        # 退出登录
GET    /auth/info          # 获取当前用户信息
```

### 文章接口

```
GET    /article/list       # 文章列表
GET    /article/{id}       # 文章详情
POST   /article/publish    # 发布文章
PUT    /article/update     # 更新文章
DELETE /article/{id}       # 删除文章
POST   /article/like/{id}  # 点赞文章
POST   /article/collect/{id} # 收藏文章
```

### 评论接口

```
GET    /comment/list       # 评论列表
POST   /comment/add        # 发表评论
DELETE /comment/{id}       # 删除评论
```

更多接口请查看各 Controller 文件

## 🎯 系统特点

1. **前后端分离** - 清晰的职责划分，便于开发和维护
2. **RESTful API** - 规范的接口设计，易于扩展
3. **权限控制** - 基于 Sa-Token 的灵活权限管理
4. **代码规范** - 统一的代码风格，良好的可读性
5. **逻辑删除** - 数据安全，支持数据恢复
6. **版本控制** - 文章历史版本管理
7. **响应式设计** - 适配移动端和桌面端

## 🔐 安全特性

- 密码 MD5 加密存储
- Token 认证机制
- Redis 会话管理
- CORS 跨域配置
- SQL 注入防护（MyBatis-Plus）
- XSS 防护

## 📈 性能优化

- Druid 数据库连接池
- Redis 缓存支持
- 分页查询优化
- 逻辑删除代替物理删除
- 索引优化

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建新分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 开源协议

本项目采用 MIT 协议开源

## 📮 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 Issue
- 发送邮件

## 🙏 鸣谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [Sa-Token](https://sa-token.cc/)
- [Naive UI](https://www.naiveui.com/)
- [Vite](https://vitejs.dev/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！⭐**

Made with ❤️ by Nebula-Hash

</div>
