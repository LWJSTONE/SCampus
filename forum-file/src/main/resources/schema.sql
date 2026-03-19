-- ============================================================
-- 论坛文件服务数据库脚本
-- 数据库: forum_file_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS `forum_file_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `forum_file_db`;

-- ----------------------------
-- 文件表
-- ----------------------------
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
    `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型（avatar-头像, post-帖子附件, comment-评论附件等）',
    `biz_id` BIGINT(20) DEFAULT NULL COMMENT '业务ID',
    `file_md5` VARCHAR(32) DEFAULT NULL COMMENT '文件MD5值（用于去重）',
    `download_count` INT(11) DEFAULT 0 COMMENT '下载次数',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态（0-正常，1-禁用）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `delete_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标志（0-未删除，1-已删除）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_name` (`file_name`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_biz` (`biz_type`, `biz_id`),
    KEY `idx_file_md5` (`file_md5`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- ----------------------------
-- 初始化数据
-- ----------------------------
INSERT INTO `sys_file` (`file_name`, `original_name`, `file_path`, `file_url`, `file_size`, `file_type`, `file_ext`, `storage_type`, `biz_type`, `download_count`, `status`) VALUES
('default_avatar.png', 'default_avatar.png', 'default/avatar.png', '/files/default/avatar.png', 1024, 'image/png', 'png', 'LOCAL', 'avatar', 0, 0);
