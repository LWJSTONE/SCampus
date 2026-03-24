-- ============================================================
-- Forum Category Service 数据库初始化脚本
-- 版块服务数据库：forum_category_db
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS forum_category_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE forum_category_db;

-- ============================================================
-- 版块分类表
-- ============================================================
DROP TABLE IF EXISTS forum_category;
CREATE TABLE forum_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    description VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    post_count INT DEFAULT 0 COMMENT '帖子数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版块分类表';

-- ============================================================
-- 版块表
-- ============================================================
DROP TABLE IF EXISTS forum_forum;
CREATE TABLE forum_forum (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '版块ID',
    name VARCHAR(100) NOT NULL COMMENT '版块名称',
    icon VARCHAR(255) DEFAULT NULL COMMENT '版块图标',
    description VARCHAR(500) DEFAULT NULL COMMENT '版块描述',
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    moderator_id BIGINT DEFAULT NULL COMMENT '版主用户ID（主要版主）',
    post_count INT NOT NULL DEFAULT 0 COMMENT '帖子数量',
    today_post_count INT NOT NULL DEFAULT 0 COMMENT '今日帖子数',
    last_post_time DATETIME DEFAULT NULL COMMENT '最后发帖时间',
    last_post_user_id BIGINT DEFAULT NULL COMMENT '最后发帖用户ID',
    last_post_title VARCHAR(200) DEFAULT NULL COMMENT '最后发帖标题',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    allow_post TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许发帖（0-否，1-是）',
    allow_reply TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许回复（0-否，1-是）',
    rules TEXT DEFAULT NULL COMMENT '版块规则',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_category_id (category_id),
    KEY idx_moderator_id (moderator_id),
    KEY idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版块表';

-- ============================================================
-- 版主关联表
-- ============================================================
DROP TABLE IF EXISTS forum_moderator;
CREATE TABLE forum_moderator (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    forum_id BIGINT NOT NULL COMMENT '版块ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名（冗余字段）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '用户昵称（冗余字段）',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '用户头像（冗余字段）',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否为主版主（0-否，1-是）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_user (forum_id, user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版主关联表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 插入默认分类
INSERT INTO forum_category (name, icon, description, parent_id, sort, status) VALUES
('校园生活', 'school', '校园生活相关讨论', 0, 1, 1),
('学习交流', 'book', '学习资料、课程讨论', 0, 2, 1),
('休闲娱乐', 'game', '娱乐、游戏、影视', 0, 3, 1),
('二手交易', 'shop', '二手物品买卖', 0, 4, 1);

-- 插入子分类
INSERT INTO forum_category (name, icon, description, parent_id, sort, status) VALUES
('新生指南', NULL, '新生入学指南', 1, 1, 1),
('社团活动', NULL, '社团活动公告', 1, 2, 1),
('课程讨论', NULL, '课程相关讨论', 2, 1, 1),
('考研考证', NULL, '考研、考证交流', 2, 2, 1);

-- 插入默认版块
INSERT INTO forum_forum (name, icon, description, category_id, sort, status, allow_post, allow_reply, rules) VALUES
('新生问答', 'question', '新生入学问题解答', 5, 1, 1, 1, 1, '请友善交流，禁止广告'),
('校园新闻', 'news', '校园新闻资讯', 5, 2, 1, 1, 1, '转载请注明出处'),
('学习资料', 'file', '学习资料分享', 7, 1, 1, 1, 1, '禁止上传侵权内容'),
('考研交流', 'exam', '考研经验分享', 8, 1, 1, 1, 1, '禁止培训机构广告'),
('游戏讨论', 'game', '游戏交流讨论', 3, 1, 1, 1, 1, '禁止外挂、代练广告'),
('二手书', 'book', '二手书籍交易', 4, 1, 1, 1, 1, '请诚信交易');
