-- =====================================================
-- SCampus 论坛系统 - 数据库表结构脚本
-- 创建时间: 2024
-- 数据库字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- 说明: 本脚本基于各微服务内的schema.sql整合生成
-- =====================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 用户服务数据库表 (forum_user_db)
-- 包含: 用户表、用户关注表、角色权限相关表
-- =====================================================

USE `forum_user_db`;

-- ---------------------------------------------------
-- 1.1 用户表 (sys_user)
-- 存储用户基本信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `bio` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `school_id` BIGINT(20) DEFAULT NULL COMMENT '学校ID',
    `student_no` VARCHAR(50) DEFAULT NULL COMMENT '学号',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `post_count` INT(11) DEFAULT 0 COMMENT '帖子数量',
    `comment_count` INT(11) DEFAULT 0 COMMENT '评论数量',
    `follower_count` INT(11) DEFAULT 0 COMMENT '粉丝数量',
    `following_count` INT(11) DEFAULT 0 COMMENT '关注数量',
    `collection_count` INT(11) DEFAULT 0 COMMENT '收藏数量',
    `login_fail_count` INT(11) DEFAULT 0 COMMENT '登录失败次数',
    `lock_time` DATETIME DEFAULT NULL COMMENT '账户锁定时间',
    `password_update_time` DATETIME DEFAULT NULL COMMENT '密码修改时间',
    `email_verified` TINYINT(1) DEFAULT 0 COMMENT '邮箱验证状态：0-未验证，1-已验证',
    `phone_verified` TINYINT(1) DEFAULT 0 COMMENT '手机验证状态：0-未验证，1-已验证',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ---------------------------------------------------
-- 1.2 用户关注表 (sys_user_follow)
-- 存储用户之间的关注关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_user_follow`;
CREATE TABLE `sys_user_follow` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
    `follower_id` BIGINT(20) NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT(20) NOT NULL COMMENT '被关注用户ID',
    `status` TINYINT(1) DEFAULT 1 COMMENT '关注状态：0-已取消，1-正常关注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`follower_id`, `following_id`),
    KEY `idx_follower_id` (`follower_id`),
    KEY `idx_following_id` (`following_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- ---------------------------------------------------
-- 1.3 角色表 (sys_role)
-- 存储系统角色信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `sort` INT(11) DEFAULT 0 COMMENT '排序',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ---------------------------------------------------
-- 1.4 权限表 (sys_permission)
-- 存储系统权限信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父权限ID',
    `permission_type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-菜单，2-按钮，3-接口',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '菜单路径',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '菜单图标',
    `sort` INT(11) DEFAULT 0 COMMENT '排序',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ---------------------------------------------------
-- 1.5 用户角色关联表 (sys_user_role)
-- 存储用户与角色的关联关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ---------------------------------------------------
-- 1.6 角色权限关联表 (sys_role_permission)
-- 存储角色与权限的关联关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT(20) NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';


-- =====================================================
-- 2. 版块服务数据库表 (forum_category_db)
-- 包含: 版块分类表、版块表、版主表
-- =====================================================

USE `forum_category_db`;

-- ---------------------------------------------------
-- 2.1 版块分类表 (forum_category)
-- 存储版块分类信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_category`;
CREATE TABLE `forum_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
    `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `post_count` INT(11) DEFAULT 0 COMMENT '帖子数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版块分类表';

-- ---------------------------------------------------
-- 2.2 版块表 (forum_forum)
-- 存储版块信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_forum`;
CREATE TABLE `forum_forum` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '版块ID',
    `name` VARCHAR(100) NOT NULL COMMENT '版块名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '版块图标',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '版块描述',
    `category_id` BIGINT(20) NOT NULL COMMENT '所属分类ID',
    `moderator_id` BIGINT(20) DEFAULT NULL COMMENT '版主用户ID（主要版主）',
    `post_count` INT(11) NOT NULL DEFAULT 0 COMMENT '帖子数量',
    `today_post_count` INT(11) NOT NULL DEFAULT 0 COMMENT '今日帖子数',
    `last_post_time` DATETIME DEFAULT NULL COMMENT '最后发帖时间',
    `last_post_user_id` BIGINT(20) DEFAULT NULL COMMENT '最后发帖用户ID',
    `last_post_title` VARCHAR(200) DEFAULT NULL COMMENT '最后发帖标题',
    `sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `allow_post` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许发帖：0-否，1-是',
    `allow_reply` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许回复：0-否，1-是',
    `rules` TEXT DEFAULT NULL COMMENT '版块规则',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_moderator_id` (`moderator_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版块表';

-- ---------------------------------------------------
-- 2.3 版主关联表 (forum_moderator)
-- 存储版块与版主的关联关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_moderator`;
CREATE TABLE `forum_moderator` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `forum_id` BIGINT(20) NOT NULL COMMENT '版块ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（冗余字段）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称（冗余字段）',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像（冗余字段）',
    `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为主版主：0-否，1-是',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_forum_user` (`forum_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版主关联表';


-- =====================================================
-- 3. 帖子服务数据库表 (forum_post_db)
-- 包含: 帖子表、标签表、帖子标签关联表、帖子附件表
-- =====================================================

USE `forum_post_db`;

-- ---------------------------------------------------
-- 3.1 板块表（帖子服务本地缓存）
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum`;
CREATE TABLE `forum` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '板块名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '板块描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '板块图标',
    `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父板块ID',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序号',
    `post_count` INT(11) DEFAULT 0 COMMENT '帖子数量',
    `status` TINYINT(4) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- ---------------------------------------------------
-- 3.2 标签表 (tag)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
    `post_count` INT(11) DEFAULT 0 COMMENT '帖子数量',
    `status` TINYINT(4) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ---------------------------------------------------
-- 3.3 帖子表 (post)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `forum_id` BIGINT(20) NOT NULL COMMENT '所属板块ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '发帖用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
    `content` LONGTEXT NOT NULL COMMENT '帖子内容（富文本）',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '帖子摘要',
    `type` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '帖子类型：0-普通帖子，1-精华帖，2-置顶帖，3-公告',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '帖子状态：0-待审核，1-已发布，2-已关闭，3-已删除',
    `view_count` INT(11) NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT(11) NOT NULL DEFAULT 0 COMMENT '评论数',
    `collect_count` INT(11) NOT NULL DEFAULT 0 COMMENT '收藏数',
    `share_count` INT(11) NOT NULL DEFAULT 0 COMMENT '分享数',
    `is_top` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `is_essence` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否精华：0-否，1-是',
    `allow_comment` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '是否允许评论：0-否，1-是',
    `top_time` DATETIME DEFAULT NULL COMMENT '置顶时间',
    `essence_time` DATETIME DEFAULT NULL COMMENT '精华时间',
    `last_reply_time` DATETIME DEFAULT NULL COMMENT '最后回复时间',
    `last_reply_user_id` BIGINT(20) DEFAULT NULL COMMENT '最后回复用户ID',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    `source_type` TINYINT(4) DEFAULT 0 COMMENT '来源类型：0-PC端，1-APP端，2-小程序',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `ip_location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
    `audit_status` TINYINT(4) DEFAULT 1 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `audit_user_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_forum_id` (`forum_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`, `delete_flag`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_last_reply_time` (`last_reply_time`),
    KEY `idx_is_top` (`is_top`),
    KEY `idx_is_essence` (`is_essence`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ---------------------------------------------------
-- 3.4 帖子标签关联表 (post_tag)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `post_tag`;
CREATE TABLE `post_tag` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `tag_id` BIGINT(20) NOT NULL COMMENT '标签ID',
    `tag_name` VARCHAR(50) DEFAULT NULL COMMENT '标签名称（冗余字段）',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- ---------------------------------------------------
-- 3.5 帖子附件表 (post_attachment)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `post_attachment`;
CREATE TABLE `post_attachment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '附件类型：1-图片，2-视频，3-音频，4-文档，5-其他',
    `name` VARCHAR(255) DEFAULT NULL COMMENT '附件名称',
    `original_name` VARCHAR(255) DEFAULT NULL COMMENT '附件原始名称',
    `url` VARCHAR(500) NOT NULL COMMENT '附件路径/URL',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图路径/URL',
    `size` BIGINT(20) DEFAULT 0 COMMENT '文件大小（字节）',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
    `width` INT(11) DEFAULT NULL COMMENT '图片宽度',
    `height` INT(11) DEFAULT NULL COMMENT '图片高度',
    `duration` INT(11) DEFAULT NULL COMMENT '时长（秒，音视频）',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序号',
    `status` TINYINT(4) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子附件表';


-- =====================================================
-- 4. 评论服务数据库表 (forum_comment_db)
-- 包含: 评论表、评论点赞表
-- =====================================================

USE `forum_comment_db`;

-- ---------------------------------------------------
-- 4.1 评论表 (forum_comment)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
    `reply_to_user_id` BIGINT(20) DEFAULT NULL COMMENT '回复目标用户ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '评论用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT(11) NOT NULL DEFAULT 0 COMMENT '回复数',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '评论状态：0-已删除，1-正常，2-被系统屏蔽',
    `is_hot` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否热门：0-否，1-是',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `ip_location` VARCHAR(100) DEFAULT NULL COMMENT 'IP归属地',
    `audit_status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`, `delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ---------------------------------------------------
-- 4.2 评论点赞表 (t_comment_like)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `t_comment_like`;
CREATE TABLE `t_comment_like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comment_id` BIGINT(20) NOT NULL COMMENT '评论ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未取消，1-已取消',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';


-- =====================================================
-- 5. 互动服务数据库表 (forum_interaction_db)
-- 包含: 点赞表、收藏表、@提及表
-- =====================================================

USE `forum_interaction_db`;

-- ---------------------------------------------------
-- 5.1 点赞表 (forum_like)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_like`;
CREATE TABLE `forum_like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `target_type` TINYINT(1) NOT NULL COMMENT '目标类型：1-帖子，2-评论',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-正常，1-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_delete_flag` (`delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- ---------------------------------------------------
-- 5.2 收藏表 (forum_collect)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_collect`;
CREATE TABLE `forum_collect` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `folder_id` BIGINT(20) DEFAULT NULL COMMENT '收藏夹ID',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-正常，1-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_delete_flag` (`delete_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ---------------------------------------------------
-- 5.3 @提及表 (forum_mention)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_mention`;
CREATE TABLE `forum_mention` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `from_user_id` BIGINT(20) NOT NULL COMMENT '发起用户ID',
    `to_user_id` BIGINT(20) NOT NULL COMMENT '被@用户ID',
    `target_type` TINYINT(1) NOT NULL COMMENT '目标类型：1-帖子，2-评论',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_from_user_id` (`from_user_id`),
    KEY `idx_to_user_id` (`to_user_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='@提及表';


-- =====================================================
-- 6. 审核服务数据库表 (forum_report_db)
-- 包含: 举报表、审核记录表、用户禁言表
-- =====================================================

USE `forum_report_db`;

-- ---------------------------------------------------
-- 6.1 举报表 (t_report)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `t_report`;
CREATE TABLE `t_report` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reporter_id` BIGINT(20) NOT NULL COMMENT '举报人ID',
    `reported_user_id` BIGINT(20) NOT NULL COMMENT '被举报人ID',
    `report_type` TINYINT(1) NOT NULL COMMENT '举报类型：1-帖子，2-评论，3-用户',
    `target_id` BIGINT(20) NOT NULL COMMENT '被举报内容ID',
    `reason_type` TINYINT(1) NOT NULL COMMENT '举报原因类型：1-垃圾广告，2-色情低俗，3-违法违规，4-人身攻击，5-恶意灌水，6-其他',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '举报原因详情',
    `images` VARCHAR(2000) DEFAULT NULL COMMENT '举报截图（JSON数组）',
    `status` TINYINT(1) DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理中，2-已处理，3-已驳回',
    `handler_id` BIGINT(20) DEFAULT NULL COMMENT '处理人ID',
    `result` TINYINT(1) DEFAULT NULL COMMENT '处理结果：0-无违规，1-警告，2-删除内容，3-禁言，4-封号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_reported_user_id` (`reported_user_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ---------------------------------------------------
-- 6.2 审核记录表 (t_approve)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `t_approve`;
CREATE TABLE `t_approve` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '关联用户ID',
    `content_type` TINYINT(1) NOT NULL COMMENT '内容类型：1-帖子，2-评论，3-头像，4-昵称',
    `content_id` BIGINT(20) NOT NULL COMMENT '内容ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '内容标题',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '内容摘要',
    `status` TINYINT(1) DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `sensitive_words` VARCHAR(500) DEFAULT NULL COMMENT '敏感词列表',
    `priority` TINYINT(1) DEFAULT 0 COMMENT '优先级：0-普通，1-重要，2-紧急',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_status` (`status`),
    KEY `idx_content_type` (`content_type`),
    KEY `idx_priority` (`priority`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- ---------------------------------------------------
-- 6.3 用户禁言表 (t_user_ban)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `t_user_ban`;
CREATE TABLE `t_user_ban` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `ban_type` TINYINT(1) DEFAULT 1 COMMENT '禁言类型：1-全站禁言，2-板块禁言',
    `forum_id` BIGINT(20) DEFAULT NULL COMMENT '板块ID（板块禁言时使用）',
    `reason` VARCHAR(500) NOT NULL COMMENT '禁言原因',
    `report_id` BIGINT(20) DEFAULT NULL COMMENT '关联举报ID',
    `operator_id` BIGINT(20) NOT NULL COMMENT '操作人ID',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT(1) DEFAULT 1 COMMENT '禁言状态：0-已解除，1-禁言中，2-已过期',
    `release_time` DATETIME DEFAULT NULL COMMENT '解除时间',
    `release_operator_id` BIGINT(20) DEFAULT NULL COMMENT '解除操作人ID',
    `release_reason` VARCHAR(500) DEFAULT NULL COMMENT '解除原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_forum_id` (`forum_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户禁言表';


-- =====================================================
-- 7. 统计服务数据库表 (forum_stats_db)
-- 包含: 每日统计表
-- =====================================================

USE `forum_stats_db`;

-- ---------------------------------------------------
-- 7.1 每日统计表 (daily_stats)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `daily_stats`;
CREATE TABLE `daily_stats` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stats_date` DATE NOT NULL COMMENT '统计日期',
    `new_users` INT(11) DEFAULT 0 COMMENT '新增用户数',
    `active_users` INT(11) DEFAULT 0 COMMENT '活跃用户数',
    `new_posts` INT(11) DEFAULT 0 COMMENT '新增帖子数',
    `new_comments` INT(11) DEFAULT 0 COMMENT '新增评论数',
    `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT(11) DEFAULT 0 COMMENT '收藏数',
    `follow_count` INT(11) DEFAULT 0 COMMENT '关注数',
    `view_count` BIGINT(20) DEFAULT 0 COMMENT '浏览量',
    `total_users` BIGINT(20) DEFAULT 0 COMMENT '总用户数（累计）',
    `total_posts` BIGINT(20) DEFAULT 0 COMMENT '总帖子数（累计）',
    `total_comments` BIGINT(20) DEFAULT 0 COMMENT '总评论数（累计）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stats_date` (`stats_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日统计表';


-- =====================================================
-- 8. 通知服务数据库表 (forum_notify_db)
-- 包含: 通知公告表、用户通知阅读表
-- =====================================================

USE `forum_notify_db`;

-- ---------------------------------------------------
-- 8.1 通知公告表 (t_notice)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知类型：1-系统公告，2-活动通知，3-版本更新，4-其他',
    `level` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知级别：1-普通，2-重要，3-紧急',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿，1-已发布，2-已撤回',
    `publisher_id` BIGINT(20) NOT NULL COMMENT '发布人ID',
    `publisher_name` VARCHAR(50) NOT NULL COMMENT '发布人名称',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `effective_start_time` DATETIME DEFAULT NULL COMMENT '生效开始时间',
    `effective_end_time` DATETIME DEFAULT NULL COMMENT '生效结束时间',
    `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `read_count` INT(11) NOT NULL DEFAULT 0 COMMENT '阅读数量',
    `attachments` VARCHAR(500) DEFAULT NULL COMMENT '附件URL（JSON格式）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- ---------------------------------------------------
-- 8.2 用户通知阅读表 (t_user_notice)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `t_user_notice`;
CREATE TABLE `t_user_notice` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `notice_id` BIGINT(20) NOT NULL COMMENT '通知ID',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（用户端删除）：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_notice` (`user_id`, `notice_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_notice_id` (`notice_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知阅读表';


-- =====================================================
-- 9. 文件服务数据库表 (forum_file_db)
-- 包含: 文件管理表
-- =====================================================

USE `forum_file_db`;

-- ---------------------------------------------------
-- 9.1 文件管理表 (sys_file)
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称（存储名称）',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(1000) DEFAULT NULL COMMENT '文件URL',
    `file_size` BIGINT(20) DEFAULT 0 COMMENT '文件大小（字节）',
    `file_type` VARCHAR(100) DEFAULT NULL COMMENT '文件类型（MIME类型）',
    `file_ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `storage_type` VARCHAR(20) DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL-本地存储，MINIO-MinIO存储',
    `uploader_id` BIGINT(20) DEFAULT NULL COMMENT '上传者ID',
    `uploader_name` VARCHAR(64) DEFAULT NULL COMMENT '上传者名称',
    `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型：avatar-头像，post-帖子附件，comment-评论附件等',
    `biz_id` BIGINT(20) DEFAULT NULL COMMENT '业务ID',
    `file_md5` VARCHAR(32) DEFAULT NULL COMMENT '文件MD5值（用于去重）',
    `download_count` INT(11) DEFAULT 0 COMMENT '下载次数',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-正常，1-禁用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_file_name` (`file_name`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_biz` (`biz_type`, `biz_id`),
    KEY `idx_file_md5` (`file_md5`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';


-- =====================================================
-- 10. 认证服务数据库表 (forum_auth_db)
-- 认证服务共享forum_user_db中的sys_user表
-- 此数据库主要用于存储认证相关的临时数据
-- =====================================================

USE `forum_auth_db`;

-- 认证服务使用forum_user_db中的sys_user表进行用户认证
-- 此处可以添加认证服务特有的表，如：
-- - 登录日志表
-- - Token黑名单表
-- - 验证码记录表

-- 如果需要独立的认证相关表，可以在此添加


SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 表结构创建完成
-- =====================================================
