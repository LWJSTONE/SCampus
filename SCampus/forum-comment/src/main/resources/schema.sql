-- ============================================================
-- 论坛评论服务数据库初始化脚本
-- 数据库名称: forum_comment_db
-- 包含表：
--   - t_comment: 评论表
--   - t_comment_like: 评论点赞表
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `forum_comment_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `forum_comment_db`;

-- ============================================================
-- 评论表
-- ============================================================
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
    `reply_to_user_id` BIGINT(20) DEFAULT NULL COMMENT '回复目标用户ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '评论用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT(11) NOT NULL DEFAULT 0 COMMENT '回复数',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '评论状态：0-正常，1-已删除，2-被屏蔽',
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
    KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_parent_id` (`parent_id`) COMMENT '父评论ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
    KEY `idx_status` (`status`, `delete_flag`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================================
-- 评论点赞表
-- ============================================================
DROP TABLE IF EXISTS `t_comment_like`;
CREATE TABLE `t_comment_like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comment_id` BIGINT(20) NOT NULL COMMENT '评论ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未取消，1-已取消',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`) COMMENT '评论用户唯一索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ============================================================
-- 初始化测试数据
-- ============================================================

-- 插入测试评论
INSERT INTO `t_comment` (`post_id`, `parent_id`, `reply_to_user_id`, `user_id`, `content`, `like_count`, `reply_count`, `status`, `is_hot`, `ip_address`, `ip_location`) VALUES
(1, 0, NULL, 1, '这是一条测试评论，帖子内容很精彩！', 10, 2, 0, 1, '127.0.0.1', '本地'),
(1, 0, NULL, 2, '支持楼主！', 5, 1, 0, 0, '127.0.0.1', '本地'),
(1, 1, 1, 3, '感谢支持！', 2, 0, 0, 0, '127.0.0.1', '本地'),
(1, 1, 1, 4, '同感！', 1, 0, 0, 0, '127.0.0.1', '本地'),
(1, 2, 2, 1, '谢谢！', 0, 0, 0, 0, '127.0.0.1', '本地');

-- 插入测试点赞
INSERT INTO `t_comment_like` (`comment_id`, `user_id`) VALUES
(1, 2),
(1, 3),
(1, 4),
(2, 1),
(2, 3);

-- ============================================================
-- 查看创建结果
-- ============================================================
SELECT '评论服务数据库初始化完成!' AS message;
SELECT COUNT(*) AS comment_count FROM t_comment;
SELECT COUNT(*) AS like_count FROM t_comment_like;
