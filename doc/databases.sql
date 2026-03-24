-- =====================================================
-- SCampus 论坛系统 - 微服务数据库创建脚本
-- 创建时间: 2024
-- 数据库字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- 说明: 本脚本基于各微服务配置文件整合生成
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =====================================================
-- 1. 用户服务数据库 (forum_user_db)
-- 端口: 9002
-- 包含: 用户表、用户关注表、角色权限相关表
-- =====================================================
DROP DATABASE IF EXISTS `forum_user_db`;
CREATE DATABASE `forum_user_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 2. 认证服务数据库 (forum_auth_db)
-- 端口: 9001
-- 说明: 认证服务共享forum_user_db中的sys_user表
--       此数据库可用于存储认证相关的临时数据
-- =====================================================
DROP DATABASE IF EXISTS `forum_auth_db`;
CREATE DATABASE `forum_auth_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 3. 版块服务数据库 (forum_category_db)
-- 端口: 9003
-- 包含: 版块分类表、版块表、版主表
-- =====================================================
DROP DATABASE IF EXISTS `forum_category_db`;
CREATE DATABASE `forum_category_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 4. 帖子服务数据库 (forum_post_db)
-- 端口: 9004
-- 包含: 帖子表、帖子标签、帖子附件、标签表、板块表
-- =====================================================
DROP DATABASE IF EXISTS `forum_post_db`;
CREATE DATABASE `forum_post_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 5. 评论服务数据库 (forum_comment_db)
-- 端口: 9005
-- 包含: 评论表、评论点赞表
-- =====================================================
DROP DATABASE IF EXISTS `forum_comment_db`;
CREATE DATABASE `forum_comment_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 6. 互动服务数据库 (forum_interaction_db)
-- 端口: 9006
-- 包含: 点赞表、收藏表、@提及表
-- =====================================================
DROP DATABASE IF EXISTS `forum_interaction_db`;
CREATE DATABASE `forum_interaction_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 7. 审核服务数据库 (forum_report_db)
-- 端口: 9007
-- 包含: 举报表、审核记录表、用户禁言表
-- =====================================================
DROP DATABASE IF EXISTS `forum_report_db`;
CREATE DATABASE `forum_report_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 8. 统计服务数据库 (forum_stats_db)
-- 端口: 9008
-- 包含: 每日统计表
-- =====================================================
DROP DATABASE IF EXISTS `forum_stats_db`;
CREATE DATABASE `forum_stats_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 9. 通知服务数据库 (forum_notify_db)
-- 端口: 9009
-- 包含: 通知公告表、用户通知阅读表
-- =====================================================
DROP DATABASE IF EXISTS `forum_notify_db`;
CREATE DATABASE `forum_notify_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 10. 文件服务数据库 (forum_file_db)
-- 端口: 9010
-- 包含: 文件管理表
-- =====================================================
DROP DATABASE IF EXISTS `forum_file_db`;
CREATE DATABASE `forum_file_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- =====================================================
-- 创建数据库用户（可选，根据实际情况调整）
-- =====================================================
-- CREATE USER 'forum_admin'@'%' IDENTIFIED BY 'forum_password_123';
-- GRANT ALL PRIVILEGES ON `forum_%`.* TO 'forum_admin'@'%';
-- FLUSH PRIVILEGES;

-- =====================================================
-- 数据库说明：
-- 1. 每个微服务使用独立数据库，实现数据隔离
-- 2. 所有数据库使用 utf8mb4 字符集，支持 emoji 表情
-- 3. 使用 utf8mb4_unicode_ci 排序规则，支持多语言排序
-- 4. 生产环境请修改默认密码
-- 5. 默认数据库连接配置：
--    - 地址: localhost:3306
--    - 用户名: root
--    - 密码: 123456 (可通过环境变量 MYSQL_PASSWORD 配置)
-- =====================================================

-- =====================================================
-- Redis 数据库分配说明：
-- forum-auth:     Redis DB 0
-- forum-user:     Redis DB 0
-- forum-category: Redis DB 0
-- forum-post:     Redis DB 0
-- forum-comment:  Redis DB 5
-- forum-interaction: Redis DB 6
-- forum-report:   Redis DB 7
-- forum-stats:    Redis DB 0
-- forum-notify:   Redis DB 9
-- forum-file:     Redis DB 7
-- =====================================================
