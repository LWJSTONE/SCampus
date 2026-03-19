-- ============================================================
-- 论坛通知服务数据库表结构
-- 
-- 数据库名称: forum_notify_db
-- 创建时间: 2024-01-01
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forum_notify_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE forum_notify_db;

-- ============================================================
-- 通知公告表
-- ============================================================
DROP TABLE IF EXISTS t_notice;
CREATE TABLE t_notice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型：1-系统公告 2-活动通知 3-版本更新 4-其他',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '通知级别：1-普通 2-重要 3-紧急',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-草稿 1-已发布 2-已撤回',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) NOT NULL COMMENT '发布人名称',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    effective_start_time DATETIME DEFAULT NULL COMMENT '生效开始时间',
    effective_end_time DATETIME DEFAULT NULL COMMENT '生效结束时间',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
    read_count INT NOT NULL DEFAULT 0 COMMENT '阅读数量',
    attachments VARCHAR(500) DEFAULT NULL COMMENT '附件URL（JSON格式）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_type (type),
    KEY idx_publish_time (publish_time),
    KEY idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- ============================================================
-- 用户通知阅读表
-- ============================================================
DROP TABLE IF EXISTS t_user_notice;
CREATE TABLE t_user_notice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（用户端删除）：0-未删除 1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_notice (user_id, notice_id),
    KEY idx_user_id (user_id),
    KEY idx_notice_id (notice_id),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知阅读表';

-- ============================================================
-- 初始化测试数据
-- ============================================================

-- 插入测试通知
INSERT INTO t_notice (title, content, type, level, status, publisher_id, publisher_name, publish_time, is_top, read_count) VALUES
('欢迎使用校园论坛系统', '欢迎使用校园论坛系统！本系统提供帖子发布、评论互动、消息通知等功能，祝您使用愉快！', 1, 1, 1, 1, '管理员', NOW(), 1, 100),
('系统升级公告', '系统将于本周六凌晨2:00-4:00进行升级维护，届时系统将暂停服务，请提前做好准备。', 3, 2, 1, 1, '管理员', NOW(), 0, 50),
('春节活动通知', '春节期间参与论坛互动，有机会获得精美礼品！活动时间：2024年1月20日-2月20日。', 2, 1, 1, 1, '管理员', NOW(), 0, 80);
