-- ============================================================
-- Forum Stats Service 数据库初始化脚本
-- 
-- 数据库名称：forum_stats_db
-- 包含表：
-- - daily_stats: 每日统计表
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forum_stats_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE forum_stats_db;

-- ============================================================
-- 每日统计表
-- ============================================================
DROP TABLE IF EXISTS `daily_stats`;
CREATE TABLE `daily_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stats_date` DATE NOT NULL COMMENT '统计日期',
    `new_users` INT DEFAULT 0 COMMENT '新增用户数',
    `active_users` INT DEFAULT 0 COMMENT '活跃用户数',
    `new_posts` INT DEFAULT 0 COMMENT '新增帖子数',
    `new_comments` INT DEFAULT 0 COMMENT '新增评论数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `follow_count` INT DEFAULT 0 COMMENT '关注数',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `total_users` BIGINT DEFAULT 0 COMMENT '总用户数（累计）',
    `total_posts` BIGINT DEFAULT 0 COMMENT '总帖子数（累计）',
    `total_comments` BIGINT DEFAULT 0 COMMENT '总评论数（累计）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stats_date` (`stats_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日统计表';

-- ============================================================
-- 插入测试数据（近7天）
-- ============================================================
INSERT INTO `daily_stats` (`stats_date`, `new_users`, `active_users`, `new_posts`, `new_comments`, `like_count`, `collect_count`, `follow_count`, `view_count`, `total_users`, `total_posts`, `total_comments`) VALUES
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 15, 120, 25, 85, 150, 45, 30, 2500, 1015, 525, 2085),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 18, 135, 30, 92, 165, 52, 28, 2800, 1033, 555, 2177),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 12, 128, 22, 78, 142, 38, 25, 2400, 1045, 577, 2255),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, 145, 35, 105, 188, 55, 32, 3200, 1065, 612, 2360),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 16, 138, 28, 88, 156, 48, 27, 2900, 1081, 640, 2448),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 22, 152, 38, 112, 195, 62, 35, 3500, 1103, 678, 2560),
(CURDATE(), 25, 160, 42, 125, 210, 70, 40, 3800, 1128, 720, 2685);
