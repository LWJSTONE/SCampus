-- ============================================================
-- 审核服务数据库初始化脚本
-- 
-- 包含表：
-- - t_report: 举报表
-- - t_approve: 审核记录表
-- - t_user_ban: 用户禁言表
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `forum_report_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `forum_report_db`;

-- ============================================================
-- 举报表
-- ============================================================
DROP TABLE IF EXISTS `t_report`;
CREATE TABLE `t_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
    `reported_user_id` BIGINT NOT NULL COMMENT '被举报人ID',
    `report_type` TINYINT NOT NULL COMMENT '举报类型: 1-帖子 2-评论 3-用户',
    `target_id` BIGINT NOT NULL COMMENT '被举报内容ID',
    `reason_type` TINYINT NOT NULL COMMENT '举报原因类型: 1-垃圾广告 2-色情低俗 3-违法违规 4-人身攻击 5-恶意灌水 6-其他',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '举报原因详情',
    `images` VARCHAR(2000) DEFAULT NULL COMMENT '举报截图（JSON数组）',
    `status` TINYINT DEFAULT 0 COMMENT '处理状态: 0-待处理 1-处理中 2-已处理 3-已驳回',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `result` TINYINT DEFAULT NULL COMMENT '处理结果: 0-无违规 1-警告 2-删除内容 3-禁言 4-封号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT DEFAULT 0 COMMENT '删除标志: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_reported_user_id` (`reported_user_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================================
-- 审核记录表
-- ============================================================
DROP TABLE IF EXISTS `t_approve`;
CREATE TABLE `t_approve` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `content_type` TINYINT NOT NULL COMMENT '内容类型: 1-帖子 2-评论 3-头像 4-昵称',
    `content_id` BIGINT NOT NULL COMMENT '内容ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '内容标题',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '内容摘要',
    `status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核 1-审核通过 2-审核拒绝',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `sensitive_words` VARCHAR(500) DEFAULT NULL COMMENT '敏感词列表',
    `priority` TINYINT DEFAULT 0 COMMENT '优先级: 0-普通 1-重要 2-紧急',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT DEFAULT 0 COMMENT '删除标志: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_status` (`status`),
    KEY `idx_content_type` (`content_type`),
    KEY `idx_priority` (`priority`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- ============================================================
-- 用户禁言表
-- ============================================================
DROP TABLE IF EXISTS `t_user_ban`;
CREATE TABLE `t_user_ban` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `ban_type` TINYINT DEFAULT 1 COMMENT '禁言类型: 1-全站禁言 2-板块禁言',
    `forum_id` BIGINT DEFAULT NULL COMMENT '板块ID（板块禁言时使用）',
    `reason` VARCHAR(500) NOT NULL COMMENT '禁言原因',
    `report_id` BIGINT DEFAULT NULL COMMENT '关联举报ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '禁言状态: 0-已解除 1-禁言中 2-已过期',
    `release_time` DATETIME DEFAULT NULL COMMENT '解除时间',
    `release_operator_id` BIGINT DEFAULT NULL COMMENT '解除操作人ID',
    `release_reason` VARCHAR(500) DEFAULT NULL COMMENT '解除原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT DEFAULT 0 COMMENT '删除标志: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_forum_id` (`forum_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户禁言表';

-- ============================================================
-- 初始测试数据（可选）
-- ============================================================

-- 插入测试举报数据
-- INSERT INTO `t_report` (`reporter_id`, `reported_user_id`, `report_type`, `target_id`, `reason_type`, `reason`, `status`)
-- VALUES (1, 2, 1, 1, 1, '垃圾广告内容', 0);

-- 插入测试审核数据
-- INSERT INTO `t_approve` (`user_id`, `content_type`, `content_id`, `title`, `content`, `status`)
-- VALUES (1, 1, 1, '测试帖子标题', '测试帖子内容摘要', 0);

-- 插入测试禁言数据
-- INSERT INTO `t_user_ban` (`user_id`, `ban_type`, `reason`, `operator_id`, `start_time`, `end_time`, `status`)
-- VALUES (2, 1, '发布违规内容', 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);
