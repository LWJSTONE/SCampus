-- =====================================================
-- SCampus 论坛系统 - 初始化数据脚本
-- 创建时间: 2024
-- 默认密码: admin123 (使用BCrypt加密)
-- 说明: 本脚本基于各微服务内的schema.sql整合生成
-- =====================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =====================================================
-- 1. 用户服务数据 (forum_user_db)
-- 包含: 用户、角色、权限
-- =====================================================

USE `forum_user_db`;

-- ---------------------------------------------------
-- 1.1 默认管理员账号
-- 密码: admin123 (BCrypt: $2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu)
-- ---------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar`, `gender`, `email`, `phone`, `bio`, `status`, `post_count`, `comment_count`, `follower_count`, `following_count`, `collection_count`, `email_verified`, `phone_verified`, `create_time`) VALUES
(1, 'admin', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '系统管理员', '/static/avatar/admin.png', 1, 'admin@scampus.com', '13800138000', '论坛系统管理员', 1, 0, 0, 0, 0, 0, 1, 1, NOW()),
(2, 'superadmin', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '超级管理员', '/static/avatar/superadmin.png', 1, 'superadmin@scampus.com', '13800138001', '超级管理员账号', 1, 0, 0, 0, 0, 0, 1, 1, NOW()),
(3, 'moderator', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '版主测试账号', '/static/avatar/moderator.png', 1, 'moderator@scampus.com', '13800138002', '版主测试账号', 1, 0, 0, 0, 0, 0, 0, 0, NOW()),
(4, 'testuser', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '测试用户', '/static/avatar/testuser.png', 2, 'testuser@scampus.com', '13800138003', '这是一个测试用户账号', 1, 0, 0, 0, 0, 0, 0, 0, NOW());

-- ---------------------------------------------------
-- 1.2 默认角色
-- ---------------------------------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `sort`, `status`, `create_time`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, 1, NOW()),
(2, '管理员', 'ADMIN', '管理系统日常运营', 2, 1, NOW()),
(3, '版主', 'MODERATOR', '管理版块内容', 3, 1, NOW()),
(4, '普通用户', 'USER', '普通注册用户', 4, 1, NOW()),
(5, '游客', 'GUEST', '未登录游客', 5, 1, NOW());

-- ---------------------------------------------------
-- 1.3 默认权限数据
-- ---------------------------------------------------
-- 一级菜单
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `status`, `create_time`) VALUES
(1, '系统管理', 'system', 0, 1, '/system', 'setting', 1, 1, NOW()),
(2, '用户管理', 'system:user', 1, 1, '/system/user', 'user', 1, 1, NOW()),
(3, '角色管理', 'system:role', 1, 1, '/system/role', 'team', 2, 1, NOW()),
(4, '权限管理', 'system:permission', 1, 1, '/system/permission', 'lock', 3, 1, NOW()),
(5, '系统配置', 'system:config', 1, 1, '/system/config', 'tool', 4, 1, NOW()),

(10, '论坛管理', 'forum', 0, 1, '/forum', 'forum', 2, 1, NOW()),
(11, '版块管理', 'forum:category', 10, 1, '/forum/category', 'appstore', 1, 1, NOW()),
(12, '帖子管理', 'forum:post', 10, 1, '/forum/post', 'file-markdown', 2, 1, NOW()),
(13, '评论管理', 'forum:comment', 10, 1, '/forum/comment', 'message', 3, 1, NOW()),
(14, '举报管理', 'forum:report', 10, 1, '/forum/report', 'warning', 4, 1, NOW()),
(15, '审核管理', 'forum:audit', 10, 1, '/forum/audit', 'audit', 5, 1, NOW()),
(16, '用户禁言', 'forum:ban', 10, 1, '/forum/ban', 'stop', 6, 1, NOW()),

(20, '内容管理', 'content', 0, 1, '/content', 'file-text', 3, 1, NOW()),
(21, '公告管理', 'content:notice', 20, 1, '/content/notice', 'notification', 1, 1, NOW()),
(22, '文件管理', 'content:file', 20, 1, '/content/file', 'folder', 2, 1, NOW()),

(30, '统计中心', 'stats', 0, 1, '/stats', 'bar-chart', 4, 1, NOW()),
(31, '数据统计', 'stats:daily', 30, 1, '/stats/daily', 'line-chart', 1, 1, NOW()),
(32, '用户统计', 'stats:user', 30, 1, '/stats/user', 'pie-chart', 2, 1, NOW()),
(33, '帖子统计', 'stats:post', 30, 1, '/stats/post', 'area-chart', 3, 1, NOW()),

(40, '个人中心', 'profile', 0, 1, '/profile', 'user', 5, 1, NOW()),
(41, '个人设置', 'profile:setting', 40, 1, '/profile/setting', 'setting', 1, 1, NOW()),
(42, '我的帖子', 'profile:post', 40, 1, '/profile/post', 'file-markdown', 2, 1, NOW()),
(43, '我的收藏', 'profile:collect', 40, 1, '/profile/collect', 'star', 3, 1, NOW()),
(44, '我的消息', 'profile:message', 40, 1, '/profile/message', 'message', 4, 1, NOW());

-- 按钮权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `parent_id`, `permission_type`, `sort`, `status`, `create_time`) VALUES
-- 用户管理按钮
('用户查询', 'system:user:list', 2, 2, 1, 1, NOW()),
('用户新增', 'system:user:add', 2, 2, 2, 1, NOW()),
('用户编辑', 'system:user:edit', 2, 2, 3, 1, NOW()),
('用户删除', 'system:user:delete', 2, 2, 4, 1, NOW()),
('重置密码', 'system:user:resetPwd', 2, 2, 5, 1, NOW()),

-- 角色管理按钮
('角色查询', 'system:role:list', 3, 2, 1, 1, NOW()),
('角色新增', 'system:role:add', 3, 2, 2, 1, NOW()),
('角色编辑', 'system:role:edit', 3, 2, 3, 1, NOW()),
('角色删除', 'system:role:delete', 3, 2, 4, 1, NOW()),

-- 版块管理按钮
('版块查询', 'forum:category:list', 11, 2, 1, 1, NOW()),
('版块新增', 'forum:category:add', 11, 2, 2, 1, NOW()),
('版块编辑', 'forum:category:edit', 11, 2, 3, 1, NOW()),
('版块删除', 'forum:category:delete', 11, 2, 4, 1, NOW()),

-- 帖子管理按钮
('帖子查询', 'forum:post:list', 12, 2, 1, 1, NOW()),
('帖子删除', 'forum:post:delete', 12, 2, 2, 1, NOW()),
('帖子置顶', 'forum:post:top', 12, 2, 3, 1, NOW()),
('帖子加精', 'forum:post:essence', 12, 2, 4, 1, NOW()),
('帖子审核', 'forum:post:audit', 12, 2, 5, 1, NOW()),

-- 评论管理按钮
('评论查询', 'forum:comment:list', 13, 2, 1, 1, NOW()),
('评论删除', 'forum:comment:delete', 13, 2, 2, 1, NOW()),
('评论审核', 'forum:comment:audit', 13, 2, 3, 1, NOW()),

-- 举报管理按钮
('举报查询', 'forum:report:list', 14, 2, 1, 1, NOW()),
('举报处理', 'forum:report:handle', 14, 2, 2, 1, NOW());

-- ---------------------------------------------------
-- 1.4 用户角色关联
-- ---------------------------------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_time`) VALUES
(1, 2, NOW()),  -- admin 是管理员
(2, 1, NOW()),  -- superadmin 是超级管理员
(3, 3, NOW()),  -- moderator 是版主
(4, 4, NOW());  -- testuser 是普通用户

-- ---------------------------------------------------
-- 1.5 角色权限关联（超级管理员拥有所有权限）
-- ---------------------------------------------------
-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 1, id, NOW() FROM `sys_permission`;

-- 管理员权限（排除用户管理、角色管理的删除权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 2, id, NOW() FROM `sys_permission` WHERE id NOT IN (104, 113);

-- 版主权限（版块管理、帖子管理、评论管理、举报处理）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(3, 11, NOW()),
(3, 12, NOW()),
(3, 13, NOW()),
(3, 14, NOW()),
(3, 15, NOW()),
(3, 16, NOW()),
(3, 120, NOW()),
(3, 130, NOW()),
(3, 131, NOW()),
(3, 132, NOW()),
(3, 133, NOW()),
(3, 134, NOW()),
(3, 135, NOW()),
(3, 140, NOW()),
(3, 141, NOW()),
(3, 142, NOW()),
(3, 150, NOW()),
(3, 151, NOW());

-- 普通用户权限（个人中心）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(4, 40, NOW()),
(4, 41, NOW()),
(4, 42, NOW()),
(4, 43, NOW()),
(4, 44, NOW());


-- =====================================================
-- 2. 版块服务数据 (forum_category_db)
-- 包含: 版块分类、版块、版主关联
-- =====================================================

USE `forum_category_db`;

-- ---------------------------------------------------
-- 2.1 版块分类
-- ---------------------------------------------------
INSERT INTO `forum_category` (`id`, `name`, `icon`, `description`, `parent_id`, `sort`, `status`, `post_count`) VALUES
-- 顶级分类
(1, '校园生活', 'school', '校园生活相关讨论', 0, 1, 1, 0),
(2, '学习交流', 'book', '学习资料、课程讨论', 0, 2, 1, 0),
(3, '休闲娱乐', 'game', '娱乐、游戏、影视', 0, 3, 1, 0),
(4, '二手交易', 'shop', '二手物品买卖', 0, 4, 1, 0),
-- 子分类
(5, '新生指南', NULL, '新生入学指南', 1, 1, 1, 0),
(6, '社团活动', NULL, '社团活动公告', 1, 2, 1, 0),
(7, '课程讨论', NULL, '课程相关讨论', 2, 1, 1, 0),
(8, '考研考证', NULL, '考研、考证交流', 2, 2, 1, 0);

-- ---------------------------------------------------
-- 2.2 版块
-- ---------------------------------------------------
INSERT INTO `forum_forum` (`id`, `name`, `icon`, `description`, `category_id`, `sort`, `status`, `allow_post`, `allow_reply`, `rules`) VALUES
(1, '新生问答', 'question', '新生入学问题解答', 5, 1, 1, 1, 1, '请友善交流，禁止广告'),
(2, '校园新闻', 'news', '校园新闻资讯', 5, 2, 1, 1, 1, '转载请注明出处'),
(3, '学习资料', 'file', '学习资料分享', 7, 1, 1, 1, 1, '禁止上传侵权内容'),
(4, '考研交流', 'exam', '考研经验分享', 8, 1, 1, 1, 1, '禁止培训机构广告'),
(5, '游戏讨论', 'game', '游戏交流讨论', 3, 1, 1, 1, 1, '禁止外挂、代练广告'),
(6, '二手书', 'book', '二手书籍交易', 4, 1, 1, 1, 1, '请诚信交易');

-- ---------------------------------------------------
-- 2.3 版主关联
-- ---------------------------------------------------
INSERT INTO `forum_moderator` (`forum_id`, `user_id`, `username`, `nickname`, `is_primary`, `status`) VALUES
(1, 3, 'moderator', '版主测试账号', 1, 1),
(3, 3, 'moderator', '版主测试账号', 1, 1);


-- =====================================================
-- 3. 帖子服务数据 (forum_post_db)
-- 包含: 板块、标签、帖子
-- =====================================================

USE `forum_post_db`;

-- ---------------------------------------------------
-- 3.1 板块数据
-- ---------------------------------------------------
INSERT INTO `forum` (`id`, `name`, `description`, `icon`, `sort_order`, `post_count`, `status`) VALUES
(1, '校园资讯', '校园新闻、活动公告', 'news', 1, 0, 1),
(2, '学习交流', '学习资料分享、学业讨论', 'study', 2, 0, 1),
(3, '生活闲谈', '日常生活、兴趣爱好', 'life', 3, 0, 1),
(4, '求职招聘', '实习招聘、职场经验', 'job', 4, 0, 1),
(5, '失物招领', '失物招领信息发布', 'lost', 5, 0, 1);

-- ---------------------------------------------------
-- 3.2 标签数据
-- ---------------------------------------------------
INSERT INTO `tag` (`id`, `name`, `color`, `post_count`, `status`) VALUES
(1, '学习', '#3498db', 0, 1),
(2, '生活', '#2ecc71', 0, 1),
(3, '求职', '#e74c3c', 0, 1),
(4, '活动', '#f39c12', 0, 1),
(5, '求助', '#9b59b6', 0, 1),
(6, '分享', '#1abc9c', 0, 1);

-- ---------------------------------------------------
-- 3.3 帖子数据
-- ---------------------------------------------------
INSERT INTO `post` (`id`, `forum_id`, `user_id`, `title`, `content`, `summary`, `type`, `status`, `view_count`, `like_count`, `comment_count`, `collect_count`, `is_top`, `is_essence`, `ip_location`, `audit_status`) VALUES
(1, 1, 1, '2024年校园运动会即将开幕', '<p>一年一度的校园运动会将于下月举行，欢迎同学们踊跃报名参加各项比赛！</p><p>报名方式：各班级体育委员处报名</p><p>报名时间：即日起至本月底</p>', '一年一度的校园运动会将于下月举行，欢迎同学们踊跃报名参加各项比赛！', 3, 1, 1256, 89, 0, 45, 1, 1, '北京', 1),
(2, 2, 2, '高等数学期末复习资料分享', '<p>整理了一份高数期末复习资料，包含重点公式和典型例题，希望对大家有帮助！</p>', '整理了一份高数期末复习资料，包含重点公式和典型例题，希望对大家有帮助！', 1, 1, 2345, 156, 0, 234, 0, 1, '上海', 1),
(3, 3, 3, '推荐几本好书', '<p>最近读了《百年孤独》和《人类简史》，非常推荐！</p><p>大家有什么好书推荐吗？欢迎评论区留言~</p>', '最近读了《百年孤独》和《人类简史》，非常推荐！大家有什么好书推荐吗？', 0, 1, 567, 34, 0, 23, 0, 0, '广州', 1),
(4, 4, 4, 'XX公司实习招聘信息', '<p>XX公司招聘暑期实习生，岗位：Java开发工程师</p><p>要求：熟悉Java基础，了解Spring框架</p><p>投递邮箱：xxx@xx.com</p>', 'XX公司招聘暑期实习生，岗位：Java开发工程师', 0, 1, 890, 67, 0, 89, 0, 0, '深圳', 1),
(5, 3, 1, '求推荐好吃的食堂', '<p>新生入学，想问问学校哪个食堂好吃？求推荐！</p>', '新生入学，想问问学校哪个食堂好吃？求推荐！', 0, 1, 234, 12, 0, 5, 0, 0, '成都', 1);

-- ---------------------------------------------------
-- 3.4 帖子标签关联
-- ---------------------------------------------------
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


-- =====================================================
-- 4. 评论服务数据 (forum_comment_db)
-- 包含: 评论、评论点赞
-- =====================================================

USE `forum_comment_db`;

-- ---------------------------------------------------
-- 4.1 测试评论
-- ---------------------------------------------------
INSERT INTO `forum_comment` (`id`, `post_id`, `parent_id`, `reply_to_user_id`, `user_id`, `content`, `like_count`, `reply_count`, `status`, `is_hot`, `ip_address`, `ip_location`, `audit_status`) VALUES
(1, 1, 0, NULL, 1, '这是一条测试评论，帖子内容很精彩！', 10, 2, 1, 1, '127.0.0.1', '本地', 1),
(2, 1, 0, NULL, 2, '支持楼主！', 5, 1, 1, 0, '127.0.0.1', '本地', 1),
(3, 1, 1, 1, 3, '感谢支持！', 2, 0, 1, 0, '127.0.0.1', '本地', 1),
(4, 1, 1, 1, 4, '同感！', 1, 0, 1, 0, '127.0.0.1', '本地', 1),
(5, 1, 2, 2, 1, '谢谢！', 0, 0, 1, 0, '127.0.0.1', '本地', 1);

-- ---------------------------------------------------
-- 4.2 测试点赞
-- ---------------------------------------------------
INSERT INTO `t_comment_like` (`comment_id`, `user_id`) VALUES
(1, 2),
(1, 3),
(1, 4),
(2, 1),
(2, 3);


-- =====================================================
-- 5. 统计服务数据 (forum_stats_db)
-- 包含: 每日统计数据
-- =====================================================

USE `forum_stats_db`;

-- ---------------------------------------------------
-- 5.1 近7天统计数据
-- ---------------------------------------------------
INSERT INTO `daily_stats` (`stats_date`, `new_users`, `active_users`, `new_posts`, `new_comments`, `like_count`, `collect_count`, `follow_count`, `view_count`, `total_users`, `total_posts`, `total_comments`) VALUES
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 15, 120, 25, 5, 150, 45, 30, 2500, 1015, 525, 2085),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 18, 135, 30, 6, 165, 52, 28, 2800, 1033, 555, 2177),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 12, 128, 22, 4, 142, 38, 25, 2400, 1045, 577, 2255),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 20, 145, 35, 7, 188, 55, 32, 3200, 1065, 612, 2360),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 16, 138, 28, 5, 156, 48, 27, 2900, 1081, 640, 2448),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 22, 152, 38, 8, 195, 62, 35, 3500, 1103, 678, 2560),
(CURDATE(), 25, 160, 42, 10, 210, 70, 40, 3800, 1128, 720, 2685);


-- =====================================================
-- 6. 通知服务数据 (forum_notify_db)
-- 包含: 通知公告
-- =====================================================

USE `forum_notify_db`;

-- ---------------------------------------------------
-- 6.1 默认公告
-- ---------------------------------------------------
INSERT INTO `t_notice` (`title`, `content`, `type`, `level`, `status`, `publisher_id`, `publisher_name`, `publish_time`, `is_top`, `read_count`) VALUES
('欢迎使用校园论坛系统', '欢迎使用校园论坛系统！本系统提供帖子发布、评论互动、消息通知等功能，祝您使用愉快！', 1, 1, 1, 1, '管理员', NOW(), 1, 100),
('系统升级公告', '系统将于本周六凌晨2:00-4:00进行升级维护，届时系统将暂停服务，请提前做好准备。', 3, 2, 1, 1, '管理员', NOW(), 0, 50),
('春节活动通知', '春节期间参与论坛互动，有机会获得精美礼品！活动时间：2024年1月20日-2月20日。', 2, 1, 1, 1, '管理员', NOW(), 0, 80);


-- =====================================================
-- 7. 文件服务数据 (forum_file_db)
-- 包含: 默认文件
-- =====================================================

USE `forum_file_db`;

-- ---------------------------------------------------
-- 7.1 默认头像
-- ---------------------------------------------------
INSERT INTO `sys_file` (`file_name`, `original_name`, `file_path`, `file_url`, `file_size`, `file_type`, `file_ext`, `storage_type`, `biz_type`, `download_count`, `status`) VALUES
('default_avatar.png', 'default_avatar.png', 'default/avatar.png', '/files/default/avatar.png', 1024, 'image/png', 'png', 'LOCAL', 'avatar', 0, 0);


-- =====================================================
-- 初始化数据完成
-- =====================================================
