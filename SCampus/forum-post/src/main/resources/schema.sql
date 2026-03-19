-- ============================================================
-- 论坛帖子服务数据库初始化脚本
-- 数据库名称: forum_post_db
-- 包含表：
--   - post: 帖子表
--   - post_tag: 帖子标签关联表
--   - post_attachment: 帖子附件表
--   - forum: 板块表（简化版）
--   - tag: 标签表（简化版）
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `forum_post_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `forum_post_db`;

-- ============================================================
-- 板块表
-- ============================================================
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
    KEY `idx_parent_id` (`parent_id`) COMMENT '父板块ID索引',
    KEY `idx_sort_order` (`sort_order`) COMMENT '排序索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- ============================================================
-- 标签表
-- ============================================================
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
    UNIQUE KEY `uk_name` (`name`) COMMENT '标签名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================================
-- 帖子表
-- ============================================================
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
    KEY `idx_forum_id` (`forum_id`) COMMENT '板块ID索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_status` (`status`, `delete_flag`) COMMENT '状态索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
    KEY `idx_last_reply_time` (`last_reply_time`) COMMENT '最后回复时间索引',
    KEY `idx_is_top` (`is_top`) COMMENT '置顶索引',
    KEY `idx_is_essence` (`is_essence`) COMMENT '精华索引',
    FULLTEXT KEY `ft_title_content` (`title`, `content`) COMMENT '标题内容全文索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ============================================================
-- 帖子标签关联表
-- ============================================================
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
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`) COMMENT '帖子标签唯一索引',
    KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引',
    KEY `idx_tag_id` (`tag_id`) COMMENT '标签ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- ============================================================
-- 帖子附件表
-- ============================================================
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
    KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引',
    KEY `idx_type` (`type`) COMMENT '类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子附件表';

-- ============================================================
-- 帖子收藏表（可选）
-- ============================================================
DROP TABLE IF EXISTS `post_collect`;
CREATE TABLE `post_collect` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`) COMMENT '帖子用户唯一索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';

-- ============================================================
-- 初始化测试数据
-- ============================================================

-- 插入板块
INSERT INTO `forum` (`id`, `name`, `description`, `icon`, `sort_order`, `post_count`, `status`) VALUES
(1, '校园资讯', '校园新闻、活动公告', 'news', 1, 0, 1),
(2, '学习交流', '学习资料分享、学业讨论', 'study', 2, 0, 1),
(3, '生活闲谈', '日常生活、兴趣爱好', 'life', 3, 0, 1),
(4, '求职招聘', '实习招聘、职场经验', 'job', 4, 0, 1),
(5, '失物招领', '失物招领信息发布', 'lost', 5, 0, 1);

-- 插入标签
INSERT INTO `tag` (`id`, `name`, `color`, `post_count`, `status`) VALUES
(1, '学习', '#3498db', 0, 1),
(2, '生活', '#2ecc71', 0, 1),
(3, '求职', '#e74c3c', 0, 1),
(4, '活动', '#f39c12', 0, 1),
(5, '求助', '#9b59b6', 0, 1),
(6, '分享', '#1abc9c', 0, 1);

-- 插入帖子
INSERT INTO `post` (`forum_id`, `user_id`, `title`, `content`, `summary`, `type`, `status`, `view_count`, `like_count`, `comment_count`, `collect_count`, `is_top`, `is_essence`, `ip_location`) VALUES
(1, 1, '2024年校园运动会即将开幕', '<p>一年一度的校园运动会将于下月举行，欢迎同学们踊跃报名参加各项比赛！</p><p>报名方式：各班级体育委员处报名</p><p>报名时间：即日起至本月底</p>', '一年一度的校园运动会将于下月举行，欢迎同学们踊跃报名参加各项比赛！', 3, 1, 1256, 89, 23, 45, 1, 1, '北京'),
(2, 2, '高等数学期末复习资料分享', '<p>整理了一份高数期末复习资料，包含重点公式和典型例题，希望对大家有帮助！</p><p>下载链接：XXX</p>', '整理了一份高数期末复习资料，包含重点公式和典型例题，希望对大家有帮助！', 1, 1, 2345, 156, 45, 234, 0, 1, '上海'),
(3, 3, '推荐几本好书', '<p>最近读了《百年孤独》和《人类简史》，非常推荐！</p><p>大家有什么好书推荐吗？欢迎评论区留言~</p>', '最近读了《百年孤独》和《人类简史》，非常推荐！大家有什么好书推荐吗？', 0, 1, 567, 34, 12, 23, 0, 0, '广州'),
(4, 4, 'XX公司实习招聘信息', '<p>XX公司招聘暑期实习生，岗位：Java开发工程师</p><p>要求：熟悉Java基础，了解Spring框架</p><p>投递邮箱：xxx@xx.com</p>', 'XX公司招聘暑期实习生，岗位：Java开发工程师', 0, 1, 890, 67, 28, 89, 0, 0, '深圳'),
(3, 5, '求推荐好吃的食堂', '<p>新生入学，想问问学校哪个食堂好吃？求推荐！</p>', '新生入学，想问问学校哪个食堂好吃？求推荐！', 0, 1, 234, 12, 34, 5, 0, 0, '成都');

-- 插入帖子标签关联
INSERT INTO `post_tag` (`post_id`, `tag_id`, `tag_name`, `sort_order`) VALUES
(1, 4, '活动', 0),
(2, 1, '学习', 0),
(2, 6, '分享', 1),
(3, 2, '生活', 0),
(3, 6, '分享', 1),
(4, 3, '求职', 0),
(5, 5, '求助', 0);

-- 更新板块帖子数
UPDATE `forum` f SET post_count = (SELECT COUNT(*) FROM post WHERE forum_id = f.id AND delete_flag = 0 AND status = 1);

-- 更新标签帖子数
UPDATE `tag` t SET post_count = (SELECT COUNT(*) FROM post_tag WHERE tag_id = t.id AND delete_flag = 0);

-- ============================================================
-- 查看创建结果
-- ============================================================
SELECT '帖子服务数据库初始化完成!' AS message;
SELECT COUNT(*) AS post_count FROM post;
SELECT COUNT(*) AS forum_count FROM forum;
SELECT COUNT(*) AS tag_count FROM tag;
