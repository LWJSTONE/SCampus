-- =====================================================
-- SCampus 论坛系统 - 微服务数据库创建脚本
-- 创建时间: 2024
-- 数据库字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =====================================================
-- 1. 用户服务数据库 (forum_user_db)
-- 包含: 用户表、用户关注表、角色权限相关表
-- =====================================================
DROP DATABASE IF EXISTS `forum_user_db`;
CREATE DATABASE `forum_user_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 2. 帖子服务数据库 (forum_post_db)
-- 包含: 帖子表、帖子标签、帖子附件、版块相关表
-- =====================================================
DROP DATABASE IF EXISTS `forum_post_db`;
CREATE DATABASE `forum_post_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 3. 评论服务数据库 (forum_comment_db)
-- 包含: 评论表、评论相关统计
-- =====================================================
DROP DATABASE IF EXISTS `forum_comment_db`;
CREATE DATABASE `forum_comment_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 4. 版块服务数据库 (forum_category_db)
-- 包含: 版块分类表、版块表、版主表
-- =====================================================
DROP DATABASE IF EXISTS `forum_category_db`;
CREATE DATABASE `forum_category_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 5. 互动服务数据库 (forum_interaction_db)
-- 包含: 点赞表、收藏表、@提及表
-- =====================================================
DROP DATABASE IF EXISTS `forum_interaction_db`;
CREATE DATABASE `forum_interaction_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 6. 审核服务数据库 (forum_report_db)
-- 包含: 举报表、审核记录表、用户禁言表
-- =====================================================
DROP DATABASE IF EXISTS `forum_report_db`;
CREATE DATABASE `forum_report_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 7. 统计服务数据库 (forum_stats_db)
-- 包含: 每日统计表、操作日志表、登录日志表
-- =====================================================
DROP DATABASE IF EXISTS `forum_stats_db`;
CREATE DATABASE `forum_stats_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 8. 通知服务数据库 (forum_notify_db)
-- 包含: 通知公告表、用户通知阅读表
-- =====================================================
DROP DATABASE IF EXISTS `forum_notify_db`;
CREATE DATABASE `forum_notify_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 9. 文件服务数据库 (forum_file_db)
-- 包含: 文件管理表
-- =====================================================
DROP DATABASE IF EXISTS `forum_file_db`;
CREATE DATABASE `forum_file_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 10. 认证服务数据库 (forum_auth_db)
-- 包含: 用户认证相关表、登录日志
-- =====================================================
DROP DATABASE IF EXISTS `forum_auth_db`;
CREATE DATABASE `forum_auth_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

-- =====================================================
-- 创建数据库用户（可选，根据实际情况调整）
-- =====================================================
-- CREATE USER 'forum_admin'@'%' IDENTIFIED BY 'forum_password_123';
-- GRANT ALL PRIVILEGES ON `forum_%`.* TO 'forum_admin'@'%';
-- FLUSH PRIVILEGES;

-- =====================================================
-- 说明：
-- 1. 每个微服务使用独立数据库，实现数据隔离
-- 2. 所有数据库使用 utf8mb4 字符集，支持 emoji 表情
-- 3. 使用 utf8mb4_general_ci 排序规则，不区分大小写
-- 4. 生产环境请修改默认密码
-- =====================================================
