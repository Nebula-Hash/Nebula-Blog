-- 创建数据库
CREATE DATABASE IF NOT EXISTS `boot4-nebula-blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `boot4-nebula-blog`;

-- =============================================
-- 用户与权限相关表
-- =============================================

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) NOT NULL COMMENT '角色标识',
  `role_sort` int(11) DEFAULT '0' COMMENT '显示顺序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `intro` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 登录日志表
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `login_location` varchar(255) DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(50) DEFAULT NULL COMMENT '浏览器',
  `os` varchar(50) DEFAULT NULL COMMENT '操作系统',
  `status` tinyint(1) DEFAULT '1' COMMENT '登录状态 0-失败 1-成功',
  `msg` varchar(255) DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` text DEFAULT NULL COMMENT '请求参数',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(255) DEFAULT NULL COMMENT '操作地点',
  `status` tinyint(1) DEFAULT '1' COMMENT '操作状态 0-失败 1-成功',
  `error_msg` text DEFAULT NULL COMMENT '错误消息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =============================================
-- 文章相关表
-- =============================================

-- 分类表
DROP TABLE IF EXISTS `blog_category`;
CREATE TABLE `blog_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `category_desc` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
DROP TABLE IF EXISTS `blog_tag`;
CREATE TABLE `blog_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 文章表
DROP TABLE IF EXISTS `blog_article`;
CREATE TABLE `blog_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '作者ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图',
  `content` longtext NOT NULL COMMENT '文章内容(Markdown)',
  `html_content` longtext DEFAULT NULL COMMENT 'HTML内容',
  `is_top` tinyint(1) DEFAULT '0' COMMENT '是否置顶 0-否 1-是',
  `is_draft` tinyint(1) DEFAULT '0' COMMENT '是否草稿 0-否 1-是',
  `audit_status` tinyint(1) DEFAULT '1' COMMENT '审核状态 0-待审核 1-审核通过 2-审核拒绝',
  `view_count` int(11) DEFAULT '0' COMMENT '浏览量',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论数',
  `collect_count` int(11) DEFAULT '0' COMMENT '收藏数',
  `version` int(11) DEFAULT '1' COMMENT '文章版本号',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章标签关联表
DROP TABLE IF EXISTS `blog_article_tag`;
CREATE TABLE `blog_article_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 文章历史版本表
DROP TABLE IF EXISTS `blog_article_history`;
CREATE TABLE `blog_article_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `content` longtext NOT NULL COMMENT '文章内容',
  `version` int(11) DEFAULT '1' COMMENT '版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章历史版本表';

-- =============================================
-- 评论互动相关表
-- =============================================

-- 评论表
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID(二级回复)',
  `reply_user_id` bigint(20) DEFAULT NULL COMMENT '回复用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数',
  `audit_status` tinyint(1) DEFAULT '1' COMMENT '审核状态 0-待审核 1-审核通过 2-审核拒绝',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 评论点赞表
DROP TABLE IF EXISTS `blog_comment_like`;
CREATE TABLE `blog_comment_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- =============================================
-- 用户互动相关表
-- =============================================

-- 文章点赞表
DROP TABLE IF EXISTS `blog_article_like`;
CREATE TABLE `blog_article_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- 文章收藏表
DROP TABLE IF EXISTS `blog_article_collect`;
CREATE TABLE `blog_article_collect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏表';

-- 浏览记录表
DROP TABLE IF EXISTS `blog_view_history`;
CREATE TABLE `blog_view_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID(未登录则为NULL)',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览记录表';

-- =============================================
-- 消息通知相关表
-- =============================================

-- 消息通知表
DROP TABLE IF EXISTS `blog_message`;
CREATE TABLE `blog_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
  `from_user_id` bigint(20) DEFAULT NULL COMMENT '发送用户ID',
  `type` tinyint(1) NOT NULL COMMENT '消息类型 1-评论 2-点赞 3-收藏 4-系统通知',
  `content` varchar(500) DEFAULT NULL COMMENT '消息内容',
  `article_id` bigint(20) DEFAULT NULL COMMENT '相关文章ID',
  `comment_id` bigint(20) DEFAULT NULL COMMENT '相关评论ID',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_from_user_id` (`from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- =============================================
-- 首页展示相关表
-- =============================================

-- 轮播图表
DROP TABLE IF EXISTS `blog_banner`;
CREATE TABLE `blog_banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT '链接URL',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入角色数据
INSERT INTO `sys_role` (`role_name`, `role_key`, `role_sort`, `status`) VALUES
('管理员', 'admin', 1, 1),
('作者', 'author', 2, 1),
('普通用户', 'user', 3, 1);

-- 插入测试用户数据（密码都是 123456 的 MD5）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `avatar`, `intro`, `role_id`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'admin@example.com', '13800138000', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '这是管理员的个人简介', 1, 1),
('author', 'e10adc3949ba59abbe56e057f20f883e', '作者小王', 'author@example.com', '13800138001', 'https://api.dicebear.com/7.x/avataaars/svg?seed=author', '热爱写作的程序员', 2, 1),
('user', 'e10adc3949ba59abbe56e057f20f883e', '普通用户', 'user@example.com', '13800138002', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user', '喜欢阅读技术文章', 3, 1);

-- 插入用户角色关联数据
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3);

-- 插入分类数据
INSERT INTO `blog_category` (`category_name`, `category_desc`, `sort`) VALUES
('Java', 'Java技术相关文章', 1),
('Spring', 'Spring框架相关文章', 2),
('前端', '前端技术相关文章', 3),
('数据库', '数据库相关文章', 4),
('算法', '算法与数据结构', 5),
('架构', '系统架构与设计', 6);

-- 插入标签数据
INSERT INTO `blog_tag` (`tag_name`) VALUES
('Spring Boot'),
('MyBatis'),
('Vue3'),
('MySQL'),
('Redis'),
('微服务'),
('分布式'),
('高并发'),
('设计模式'),
('算法');

-- 插入轮播图数据
INSERT INTO `blog_banner` (`title`, `image_url`, `link_url`, `sort`, `status`) VALUES
('欢迎来到博客系统', 'https://picsum.photos/1920/500?random=1', '/article/1', 1, 1),
('技术分享平台', 'https://picsum.photos/1920/500?random=2', '/article/2', 2, 1),
('知识创造价值', 'https://picsum.photos/1920/500?random=3', '/article/3', 3, 1);

-- 插入文章示例数据
INSERT INTO `blog_article` (`user_id`, `category_id`, `title`, `summary`, `cover_image`, `content`, `is_top`, `is_draft`, `view_count`, `like_count`, `comment_count`) VALUES
(2, 1, 'Spring Boot 4.0 入门教程', 'Spring Boot 4.0是Spring框架的最新版本,本文将带你快速入门', 'https://picsum.photos/800/600?random=10', '# Spring Boot 4.0 入门教程\n\n## 简介\n\nSpring Boot 4.0 是 Spring 框架的最新版本...\n\n## 快速开始\n\n1. 创建项目\n2. 配置依赖\n3. 编写代码\n\n## 总结\n\n本文介绍了Spring Boot 4.0的基本使用...', 1, 0, 156, 23, 5),
(2, 3, 'Vue3 组合式API完全指南', 'Vue3带来了全新的组合式API,让代码更加优雅和可维护', 'https://picsum.photos/800/600?random=11', '# Vue3 组合式API完全指南\n\n## 什么是组合式API\n\n组合式API是Vue3的核心特性...\n\n## 基本使用\n\n```js\nsetup() {\n  // your code\n}\n```', 0, 0, 234, 45, 12),
(1, 2, 'MyBatis-Plus实战教程', 'MyBatis-Plus为简化开发而生,让你的CRUD操作更加简单', 'https://picsum.photos/800/600?random=12', '# MyBatis-Plus实战教程\n\n## 简介\n\nMyBatis-Plus是一个MyBatis的增强工具...', 0, 0, 189, 34, 8);

-- 插入文章标签关联数据
INSERT INTO `blog_article_tag` (`article_id`, `tag_id`) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 2),
(3, 4);

-- 插入评论示例数据
INSERT INTO `blog_comment` (`article_id`, `user_id`, `content`, `like_count`) VALUES
(1, 3, '写得真好,学习了!', 5),
(1, 1, '感谢分享,很有帮助', 3),
(2, 3, 'Vue3确实很强大', 8);

-- 插入二级评论
INSERT INTO `blog_comment` (`article_id`, `user_id`, `parent_id`, `reply_user_id`, `content`, `like_count`) VALUES
(1, 2, 1, 3, '谢谢支持!', 2);
