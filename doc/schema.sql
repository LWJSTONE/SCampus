-- =====================================================
-- SCampus 论坛系统 - 数据库表结构脚本
-- 创建时间: 2024
-- 数据库字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- =====================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 系统基础表 (sys_ 前缀)
-- =====================================================

-- ---------------------------------------------------
-- 1. 用户表 (sys_user)
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
    `signature` VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
    `school` VARCHAR(100) DEFAULT NULL COMMENT '学校',
    `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
    `experience` INT(11) DEFAULT 0 COMMENT '经验值',
    `integral` INT(11) DEFAULT 0 COMMENT '积分',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常，2-封禁',
    `ban_expire_time` DATETIME DEFAULT NULL COMMENT '封禁到期时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ---------------------------------------------------
-- 2. 用户关注表 (sys_user_follow)
-- 存储用户之间的关注关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_user_follow`;
CREATE TABLE `sys_user_follow` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（关注者）',
    `follow_user_id` BIGINT(20) NOT NULL COMMENT '被关注用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户关注表';

-- ---------------------------------------------------
-- 3. 角色表 (sys_role)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ---------------------------------------------------
-- 4. 权限表 (sys_permission)
-- 存储系统权限信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父权限ID',
    `type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-菜单，2-按钮，3-接口',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- ---------------------------------------------------
-- 5. 用户角色关联表 (sys_user_role)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- ---------------------------------------------------
-- 6. 角色权限关联表 (sys_role_permission)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- ---------------------------------------------------
-- 7. 操作日志表 (sys_operation_log)
-- 存储用户操作日志
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作内容',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '操作地点',
    `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `execute_time` INT(11) DEFAULT 0 COMMENT '执行时长(ms)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ---------------------------------------------------
-- 8. 登录日志表 (sys_login_log)
-- 存储用户登录日志
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `login_type` VARCHAR(20) DEFAULT NULL COMMENT '登录方式：password-密码，sms-短信，oauth-第三方',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `message` VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

-- ---------------------------------------------------
-- 9. 系统配置表 (sys_config)
-- 存储系统配置信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    `config_type` VARCHAR(50) DEFAULT 'text' COMMENT '配置类型：text-文本，number-数字，boolean-布尔，json-JSON',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

-- ---------------------------------------------------
-- 10. 数据字典表 (sys_dict)
-- 存储数据字典
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父字典ID',
    `sort` INT(11) DEFAULT 0 COMMENT '排序',
    `css_class` VARCHAR(100) DEFAULT NULL COMMENT 'CSS样式',
    `list_class` VARCHAR(100) DEFAULT NULL COMMENT '列表样式',
    `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据字典表';

-- ---------------------------------------------------
-- 11. 文件管理表 (sys_file)
-- 存储文件信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
    `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    `file_size` BIGINT(20) DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    `file_ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `md5` VARCHAR(32) DEFAULT NULL COMMENT '文件MD5',
    `storage_type` VARCHAR(20) DEFAULT 'local' COMMENT '存储类型：local-本地，oss-阿里云OSS，cos-腾讯云COS',
    `bucket_name` VARCHAR(100) DEFAULT NULL COMMENT '存储桶名称',
    `uploader_id` BIGINT(20) DEFAULT NULL COMMENT '上传者ID',
    `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    `business_id` BIGINT(20) DEFAULT NULL COMMENT '业务ID',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_md5` (`md5`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_business` (`business_type`, `business_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件管理表';

-- ---------------------------------------------------
-- 12. 通知公告表 (sys_notice)
-- 存储系统通知公告
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `notice_title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `notice_content` TEXT COMMENT '通知内容',
    `notice_type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-通知，2-公告',
    `notice_level` TINYINT(1) DEFAULT 1 COMMENT '级别：1-普通，2-重要，3-紧急',
    `target_type` TINYINT(1) DEFAULT 1 COMMENT '目标类型：1-全部用户，2-指定用户，3-指定角色',
    `target_ids` VARCHAR(500) DEFAULT NULL COMMENT '目标ID列表',
    `sender_id` BIGINT(20) DEFAULT NULL COMMENT '发送者ID',
    `is_top` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已撤回',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_notice_type` (`notice_type`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';

-- ---------------------------------------------------
-- 13. 用户通知阅读表 (sys_user_notice)
-- 存储用户通知阅读状态
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_user_notice`;
CREATE TABLE `sys_user_notice` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `notice_id` BIGINT(20) NOT NULL COMMENT '通知ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户通知阅读表';

-- ---------------------------------------------------
-- 14. 敏感词表 (sys_sensitive_word)
-- 存储敏感词信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `sys_sensitive_word`;
CREATE TABLE `sys_sensitive_word` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
    `word_type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-违禁词，2-敏感词，3-警告词',
    `replace_word` VARCHAR(100) DEFAULT NULL COMMENT '替换词',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`),
    KEY `idx_word_type` (`word_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词表';


-- =====================================================
-- 论坛业务表 (forum_ 前缀)
-- =====================================================

-- ---------------------------------------------------
-- 15. 版块分类表 (forum_category)
-- 存储版块分类信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_category`;
CREATE TABLE `forum_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `category_icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `category_desc` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    `sort` INT(11) DEFAULT 0 COMMENT '排序',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='版块分类表';

-- ---------------------------------------------------
-- 16. 版块表 (forum_forum)
-- 存储版块信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_forum`;
CREATE TABLE `forum_forum` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '版块ID',
    `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
    `forum_name` VARCHAR(100) NOT NULL COMMENT '版块名称',
    `forum_icon` VARCHAR(255) DEFAULT NULL COMMENT '版块图标',
    `forum_desc` VARCHAR(500) DEFAULT NULL COMMENT '版块描述',
    `forum_rules` TEXT COMMENT '版块规则',
    `post_count` INT(11) DEFAULT 0 COMMENT '帖子数量',
    `comment_count` INT(11) DEFAULT 0 COMMENT '评论数量',
    `today_post_count` INT(11) DEFAULT 0 COMMENT '今日发帖数',
    `today_comment_count` INT(11) DEFAULT 0 COMMENT '今日评论数',
    `last_post_id` BIGINT(20) DEFAULT NULL COMMENT '最后帖子ID',
    `last_post_time` DATETIME DEFAULT NULL COMMENT '最后发帖时间',
    `last_post_user_id` BIGINT(20) DEFAULT NULL COMMENT '最后发帖用户ID',
    `sort` INT(11) DEFAULT 0 COMMENT '排序',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='版块表';

-- ---------------------------------------------------
-- 17. 版主关联表 (forum_moderator)
-- 存储版块与版主的关联关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_moderator`;
CREATE TABLE `forum_moderator` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `forum_id` BIGINT(20) NOT NULL COMMENT '版块ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `moderator_type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-版主，2-副版主',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_forum_user` (`forum_id`, `user_id`),
    KEY `idx_forum_id` (`forum_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='版主关联表';

-- ---------------------------------------------------
-- 18. 帖子表 (forum_post)
-- 存储帖子信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `forum_id` BIGINT(20) NOT NULL COMMENT '版块ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` LONGTEXT COMMENT '内容',
    `content_type` TINYINT(1) DEFAULT 1 COMMENT '内容类型：1-普通文本，2-Markdown，3-富文本',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `post_type` TINYINT(1) DEFAULT 1 COMMENT '帖子类型：1-普通帖，2-精华帖，3-置顶帖',
    `is_original` TINYINT(1) DEFAULT 1 COMMENT '是否原创：0-否，1-是',
    `is_anonymous` TINYINT(1) DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    `view_count` INT(11) DEFAULT 0 COMMENT '浏览数',
    `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT(11) DEFAULT 0 COMMENT '评论数',
    `collect_count` INT(11) DEFAULT 0 COMMENT '收藏数',
    `share_count` INT(11) DEFAULT 0 COMMENT '分享数',
    `is_essence` TINYINT(1) DEFAULT 0 COMMENT '是否精华：0-否，1-是',
    `is_top` TINYINT(1) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `top_level` INT(11) DEFAULT 0 COMMENT '置顶级别：0-不置顶，1-本版置顶，2-全局置顶',
    `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否热门：0-否，1-是',
    `is_locked` TINYINT(1) DEFAULT 0 COMMENT '是否锁定：0-否，1-是',
    `last_comment_id` BIGINT(20) DEFAULT NULL COMMENT '最后评论ID',
    `last_comment_time` DATETIME DEFAULT NULL COMMENT '最后评论时间',
    `last_comment_user_id` BIGINT(20) DEFAULT NULL COMMENT '最后评论用户ID',
    `audit_status` TINYINT(1) DEFAULT 1 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
    `audit_user_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '发帖IP',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已删除，3-已屏蔽',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_forum_id` (`forum_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_is_top` (`is_top`),
    KEY `idx_is_essence` (`is_essence`),
    KEY `idx_is_hot` (`is_hot`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_last_comment_time` (`last_comment_time`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子表';

-- ---------------------------------------------------
-- 19. 帖子标签关联表 (forum_post_tag)
-- 存储帖子与标签的关联关系
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_post_tag`;
CREATE TABLE `forum_post_tag` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `tag_id` BIGINT(20) NOT NULL COMMENT '标签ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子标签关联表';

-- ---------------------------------------------------
-- 20. 帖子附件表 (forum_post_attachment)
-- 存储帖子附件信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_post_attachment`;
CREATE TABLE `forum_post_attachment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `file_id` BIGINT(20) NOT NULL COMMENT '文件ID',
    `attachment_type` TINYINT(1) DEFAULT 1 COMMENT '类型：1-图片，2-视频，3-音频，4-文档，5-其他',
    `sort` INT(11) DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子附件表';

-- ---------------------------------------------------
-- 21. 评论表 (forum_comment)
-- 存储评论信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '评论用户ID',
    `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
    `reply_user_id` BIGINT(20) DEFAULT NULL COMMENT '回复用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT(11) DEFAULT 0 COMMENT '回复数',
    `is_author` TINYINT(1) DEFAULT 0 COMMENT '是否楼主：0-否，1-是',
    `is_anonymous` TINYINT(1) DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否热门评论：0-否，1-是',
    `audit_status` TINYINT(1) DEFAULT 1 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
    `audit_user_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '评论IP',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-删除，1-正常，2-屏蔽',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- ---------------------------------------------------
-- 22. 点赞表 (forum_like)
-- 存储点赞信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_like`;
CREATE TABLE `forum_like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `target_type` TINYINT(1) NOT NULL COMMENT '目标类型：1-帖子，2-评论',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='点赞表';

-- ---------------------------------------------------
-- 23. 收藏表 (forum_collect)
-- 存储收藏信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_collect`;
CREATE TABLE `forum_collect` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `post_id` BIGINT(20) NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏表';

-- ---------------------------------------------------
-- 24. @提及表 (forum_mention)
-- 存储@提及信息
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='@提及表';

-- ---------------------------------------------------
-- 25. 举报表 (forum_report)
-- 存储举报信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_report`;
CREATE TABLE `forum_report` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `report_user_id` BIGINT(20) NOT NULL COMMENT '举报人ID',
    `target_type` TINYINT(1) NOT NULL COMMENT '目标类型：1-帖子，2-评论，3-用户',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `report_type` TINYINT(1) NOT NULL COMMENT '举报类型：1-广告，2-涉政，3-涉黄，4-涉暴，5-侵权，6-辱骂，7-其他',
    `report_reason` VARCHAR(500) DEFAULT NULL COMMENT '举报原因',
    `report_images` VARCHAR(1000) DEFAULT NULL COMMENT '举报图片(JSON)',
    `handle_status` TINYINT(1) DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理中，2-已处理',
    `handle_user_id` BIGINT(20) DEFAULT NULL COMMENT '处理人ID',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `handle_result` TINYINT(1) DEFAULT NULL COMMENT '处理结果：1-无效举报，2-警告，3-删除内容，4-禁言用户，5-封禁用户',
    `handle_remark` VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_user_id` (`report_user_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_handle_status` (`handle_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='举报表';

-- ---------------------------------------------------
-- 26. 审核记录表 (forum_approve)
-- 存储审核记录
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_approve`;
CREATE TABLE `forum_approve` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '审核ID',
    `target_type` TINYINT(1) NOT NULL COMMENT '目标类型：1-帖子，2-评论',
    `target_id` BIGINT(20) NOT NULL COMMENT '目标ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '提交用户ID',
    `before_status` TINYINT(1) DEFAULT NULL COMMENT '审核前状态',
    `after_status` TINYINT(1) NOT NULL COMMENT '审核后状态',
    `approve_result` TINYINT(1) NOT NULL COMMENT '审核结果：1-通过，2-拒绝',
    `approve_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    `approve_user_id` BIGINT(20) NOT NULL COMMENT '审核人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_approve_user_id` (`approve_user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核记录表';

-- ---------------------------------------------------
-- 27. 用户禁言表 (forum_user_ban)
-- 存储用户禁言信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_user_ban`;
CREATE TABLE `forum_user_ban` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '禁言ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `ban_type` TINYINT(1) DEFAULT 1 COMMENT '禁言类型：1-全站禁言，2-版块禁言',
    `forum_id` BIGINT(20) DEFAULT NULL COMMENT '版块ID（版块禁言时有效）',
    `ban_reason` VARCHAR(255) DEFAULT NULL COMMENT '禁言原因',
    `ban_duration` INT(11) DEFAULT 0 COMMENT '禁言时长(小时)，0表示永久',
    `ban_user_id` BIGINT(20) NOT NULL COMMENT '操作人ID',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `unban_time` DATETIME DEFAULT NULL COMMENT '解禁时间',
    `unban_user_id` BIGINT(20) DEFAULT NULL COMMENT '解禁操作人ID',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-已解禁，1-禁言中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_forum_id` (`forum_id`),
    KEY `idx_status` (`status`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户禁言表';

-- ---------------------------------------------------
-- 28. 每日统计表 (forum_daily_stats)
-- 存储每日统计数据
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_daily_stats`;
CREATE TABLE `forum_daily_stats` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `stats_date` DATE NOT NULL COMMENT '统计日期',
    `new_user_count` INT(11) DEFAULT 0 COMMENT '新增用户数',
    `active_user_count` INT(11) DEFAULT 0 COMMENT '活跃用户数',
    `new_post_count` INT(11) DEFAULT 0 COMMENT '新增帖子数',
    `new_comment_count` INT(11) DEFAULT 0 COMMENT '新增评论数',
    `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT(11) DEFAULT 0 COMMENT '收藏数',
    `report_count` INT(11) DEFAULT 0 COMMENT '举报数',
    `report_handled_count` INT(11) DEFAULT 0 COMMENT '举报处理数',
    `forum_stats` TEXT COMMENT '版块统计(JSON)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stats_date` (`stats_date`),
    KEY `idx_stats_date` (`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='每日统计表';

-- ---------------------------------------------------
-- 29. 标签表 (forum_tag)
-- 存储标签信息
-- ---------------------------------------------------
DROP TABLE IF EXISTS `forum_tag`;
CREATE TABLE `forum_tag` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `tag_color` VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
    `use_count` INT(11) DEFAULT 0 COMMENT '使用次数',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`),
    KEY `idx_use_count` (`use_count`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 表结构创建完成
-- =====================================================
