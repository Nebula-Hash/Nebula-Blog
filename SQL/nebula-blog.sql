/*
 Navicat Premium Dump SQL

 Source Server         : Local_MySQL
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : nebula-blog

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 22/01/2026 15:11:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blog_article
-- ----------------------------
DROP TABLE IF EXISTS `blog_article`;
CREATE TABLE `blog_article`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章内容(Markdown)',
  `html_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'HTML内容',
  `is_top` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
  `is_draft` tinyint(1) NULL DEFAULT 0 COMMENT '是否草稿 0-否 1-是',
  `audit_status` tinyint(1) NULL DEFAULT 1 COMMENT '审核状态 0-待审核 1-审核通过 2-审核拒绝',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `collect_count` int NULL DEFAULT 0 COMMENT '收藏数',
  `version` int NULL DEFAULT 1 COMMENT '文章版本号',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_is_top`(`is_top` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_article
-- ----------------------------
INSERT INTO `blog_article` VALUES (1, 2, 1, 'Spring Boot 4.0 入门教程', 'Spring Boot 4.0是Spring框架的最新版本,本文将带你快速入门', 'https://picsum.photos/800/600?random=10', '# Spring Boot 4.0 入门教程\n\n## 简介\n\nSpring Boot 4.0 是 Spring 框架的最新版本...\n\n## 快速开始\n\n1. 创建项目\n2. 配置依赖\n3. 编写代码\n\n## 总结\n\n本文介绍了Spring Boot 4.0的基本使用...', NULL, 1, 0, 1, 162, 23, 5, 0, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_article` VALUES (2, 2, 3, 'Vue3 组合式API完全指南', 'Vue3带来了全新的组合式API,让代码更加优雅和可维护', 'https://picsum.photos/800/600?random=11', '# Vue3 组合式API完全指南\n\n## 什么是组合式API\n\n组合式API是Vue3的核心特性...\n\n## 基本使用\n\n```js\nsetup() {\n  // your code\n}\n```', NULL, 0, 0, 1, 240, 45, 12, 0, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_article` VALUES (3, 1, 2, 'MyBatis-Plus实战教程', 'MyBatis-Plus为简化开发而生,让你的CRUD操作更加简单', 'https://picsum.photos/800/600?random=12', '# MyBatis-Plus实战教程\n\n## 简介\n\nMyBatis-Plus是一个MyBatis的增强工具...', NULL, 0, 0, 1, 190, 34, 8, 0, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for blog_article_collect
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_collect`;
CREATE TABLE `blog_article_collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_user`(`article_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_article_collect
-- ----------------------------

-- ----------------------------
-- Table structure for blog_article_history
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_history`;
CREATE TABLE `blog_article_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章内容',
  `version` int NULL DEFAULT 1 COMMENT '版本号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章历史版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_article_history
-- ----------------------------

-- ----------------------------
-- Table structure for blog_article_like
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_like`;
CREATE TABLE `blog_article_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_user`(`article_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_article_like
-- ----------------------------

-- ----------------------------
-- Table structure for blog_article_tag
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_tag`;
CREATE TABLE `blog_article_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_article_tag
-- ----------------------------
INSERT INTO `blog_article_tag` VALUES (1, 1, 1, '2025-12-02 20:54:12');
INSERT INTO `blog_article_tag` VALUES (2, 1, 2, '2025-12-02 20:54:12');
INSERT INTO `blog_article_tag` VALUES (3, 2, 3, '2025-12-02 20:54:12');
INSERT INTO `blog_article_tag` VALUES (4, 3, 2, '2025-12-02 20:54:12');
INSERT INTO `blog_article_tag` VALUES (5, 3, 4, '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for blog_banner
-- ----------------------------
DROP TABLE IF EXISTS `blog_banner`;
CREATE TABLE `blog_banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接URL',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_banner
-- ----------------------------
INSERT INTO `blog_banner` VALUES (1, '欢迎来到博客系统', 'https://picsum.photos/1920/500?random=1', '/article/1', 1, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_banner` VALUES (2, '技术分享平台', 'https://picsum.photos/1920/500?random=2', '/article/2', 2, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_banner` VALUES (3, '知识创造价值', 'https://picsum.photos/1920/500?random=3', '/article/3', 3, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for blog_category
-- ----------------------------
DROP TABLE IF EXISTS `blog_category`;
CREATE TABLE `blog_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `category_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类描述',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_category
-- ----------------------------
INSERT INTO `blog_category` VALUES (1, 'Java', 'Java技术相关文章', 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_category` VALUES (2, 'Spring', 'Spring框架相关文章', 2, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_category` VALUES (3, '前端', '前端技术相关文章', 3, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_category` VALUES (4, '数据库', '数据库相关文章', 4, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_category` VALUES (5, '算法', '算法与数据结构', 5, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_category` VALUES (6, '架构', '系统架构与设计', 6, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for blog_comment
-- ----------------------------
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID(二级回复)',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `audit_status` tinyint(1) NULL DEFAULT 1 COMMENT '审核状态 0-待审核 1-审核通过 2-审核拒绝',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_comment
-- ----------------------------
INSERT INTO `blog_comment` VALUES (1, 1, 3, NULL, NULL, '写得真好,学习了!', 5, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_comment` VALUES (2, 1, 1, NULL, NULL, '感谢分享,很有帮助', 3, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_comment` VALUES (3, 2, 3, NULL, NULL, 'Vue3确实很强大', 8, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_comment` VALUES (4, 1, 2, 1, 3, '谢谢支持!', 2, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for blog_comment_like
-- ----------------------------
DROP TABLE IF EXISTS `blog_comment_like`;
CREATE TABLE `blog_comment_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_comment_like
-- ----------------------------

-- ----------------------------
-- Table structure for blog_message
-- ----------------------------
DROP TABLE IF EXISTS `blog_message`;
CREATE TABLE `blog_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `from_user_id` bigint NULL DEFAULT NULL COMMENT '发送用户ID',
  `type` tinyint(1) NOT NULL COMMENT '消息类型 1-评论 2-点赞 3-收藏 4-系统通知',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息内容',
  `article_id` bigint NULL DEFAULT NULL COMMENT '相关文章ID',
  `comment_id` bigint NULL DEFAULT NULL COMMENT '相关评论ID',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_from_user_id`(`from_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_message
-- ----------------------------

-- ----------------------------
-- Table structure for blog_tag
-- ----------------------------
DROP TABLE IF EXISTS `blog_tag`;
CREATE TABLE `blog_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_tag
-- ----------------------------
INSERT INTO `blog_tag` VALUES (1, 'Spring Boot', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (2, 'MyBatis', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (3, 'Vue3', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (4, 'MySQL', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (5, 'Redis', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (6, '微服务', 1, '2025-12-02 20:54:12', '2025-12-29 18:06:23');
INSERT INTO `blog_tag` VALUES (7, '分布式', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (8, '高并发', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (9, '设计模式', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `blog_tag` VALUES (10, '算法', 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for blog_view_history
-- ----------------------------
DROP TABLE IF EXISTS `blog_view_history`;
CREATE TABLE `blog_view_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID(未登录则为NULL)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浏览记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_view_history
-- ----------------------------
INSERT INTO `blog_view_history` VALUES (1, 2, NULL, NULL, '2025-12-02 22:46:13');
INSERT INTO `blog_view_history` VALUES (2, 1, NULL, NULL, '2025-12-23 16:19:25');
INSERT INTO `blog_view_history` VALUES (3, 1, NULL, NULL, '2025-12-23 16:25:40');
INSERT INTO `blog_view_history` VALUES (4, 2, NULL, NULL, '2025-12-23 16:25:55');
INSERT INTO `blog_view_history` VALUES (5, 2, NULL, NULL, '2025-12-23 16:26:28');
INSERT INTO `blog_view_history` VALUES (6, 2, NULL, NULL, '2025-12-23 16:26:30');
INSERT INTO `blog_view_history` VALUES (7, 2, NULL, NULL, '2025-12-29 18:07:54');
INSERT INTO `blog_view_history` VALUES (8, 3, NULL, NULL, '2025-12-29 18:08:13');
INSERT INTO `blog_view_history` VALUES (9, 1, NULL, NULL, '2025-12-29 18:10:28');
INSERT INTO `blog_view_history` VALUES (10, 1, NULL, NULL, '2025-12-29 18:12:46');
INSERT INTO `blog_view_history` VALUES (11, 1, NULL, NULL, '2026-01-07 16:30:55');
INSERT INTO `blog_view_history` VALUES (12, 2, NULL, NULL, '2026-01-14 11:12:18');
INSERT INTO `blog_view_history` VALUES (13, 1, NULL, NULL, '2026-01-14 11:12:25');

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '登录状态 0-失败 1-成功',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作地点',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '操作状态 0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误消息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `role_sort` int NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'admin', 1, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `sys_role` VALUES (2, '作者', 'author', 2, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `sys_role` VALUES (3, '普通用户', 'user', 3, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `intro` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'admin@example.com', '13800138000', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '这是管理员的个人简介', 1, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `sys_user` VALUES (2, 'author', 'e10adc3949ba59abbe56e057f20f883e', '作者小王', 'author@example.com', '13800138001', 'https://api.dicebear.com/7.x/avataaars/svg?seed=author', '热爱写作的程序员', 2, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');
INSERT INTO `sys_user` VALUES (3, 'user', 'e10adc3949ba59abbe56e057f20f883e', '普通用户', 'user@example.com', '13800138002', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user', '喜欢阅读技术文章', 3, 1, 0, '2025-12-02 20:54:12', '2025-12-02 20:54:12');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2025-12-02 20:54:12');
INSERT INTO `sys_user_role` VALUES (2, 2, 2, '2025-12-02 20:54:12');
INSERT INTO `sys_user_role` VALUES (3, 3, 3, '2025-12-02 20:54:12');

SET FOREIGN_KEY_CHECKS = 1;
