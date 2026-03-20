-- =====================================================
-- SCampus 论坛系统 - 初始化数据脚本
-- 创建时间: 2024
-- 默认密码: admin123 (使用BCrypt加密)
-- =====================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =====================================================
-- 1. 用户服务数据 (forum_user_db)
-- 包含: 用户、角色、权限、配置、字典、敏感词
-- =====================================================

USE `forum_user_db`;

-- ---------------------------------------------------
-- 1.1 默认管理员账号
-- 密码: admin123 (BCrypt: $2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu)
-- ---------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar`, `gender`, `email`, `phone`, `signature`, `school`, `grade`, `experience`, `integral`, `status`, `create_time`) VALUES
(1, 'admin', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '系统管理员', '/static/avatar/admin.png', 1, 'admin@scampus.com', '13800138000', '论坛系统管理员', 'SCampus大学', '2024级', 9999, 9999, 1, NOW()),
(2, 'superadmin', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '超级管理员', '/static/avatar/superadmin.png', 1, 'superadmin@scampus.com', '13800138001', '超级管理员账号', 'SCampus大学', '2024级', 9999, 9999, 1, NOW()),
(3, 'moderator', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '版主测试账号', '/static/avatar/moderator.png', 1, 'moderator@scampus.com', '13800138002', '版主测试账号', 'SCampus大学', '2023级', 1000, 500, 1, NOW()),
(4, 'testuser', '$2a$10$/3ZkFMrKrB/X6wZhUit/dOthjcucMOssbCoPt7UbkNsCow0g.nevu', '测试用户', '/static/avatar/testuser.png', 2, 'testuser@scampus.com', '13800138003', '这是一个测试用户账号', 'SCampus大学', '2022级', 100, 50, 1, NOW());

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
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `icon`, `sort`, `status`, `create_time`) VALUES
(1, '系统管理', 'system', 0, 1, '/system', 'setting', 1, 1, NOW()),
(2, '用户管理', 'system:user', 1, 1, '/system/user', 'user', 1, 1, NOW()),
(3, '角色管理', 'system:role', 1, 1, '/system/role', 'team', 2, 1, NOW()),
(4, '权限管理', 'system:permission', 1, 1, '/system/permission', 'lock', 3, 1, NOW()),
(5, '操作日志', 'system:log', 1, 1, '/system/log', 'file-text', 4, 1, NOW()),
(6, '系统配置', 'system:config', 1, 1, '/system/config', 'tool', 5, 1, NOW()),
(7, '数据字典', 'system:dict', 1, 1, '/system/dict', 'book', 6, 1, NOW()),
(8, '敏感词管理', 'system:sensitive', 1, 1, '/system/sensitive', 'stop', 7, 1, NOW()),

(10, '论坛管理', 'forum', 0, 1, '/forum', 'forum', 2, 1, NOW()),
(11, '版块管理', 'forum:category', 10, 1, '/forum/category', 'appstore', 1, 1, NOW()),
(12, '帖子管理', 'forum:post', 10, 1, '/forum/post', 'file-markdown', 2, 1, NOW()),
(13, '评论管理', 'forum:comment', 10, 1, '/forum/comment', 'message', 3, 1, NOW()),
(14, '举报管理', 'forum:report', 10, 1, '/forum/report', 'warning', 4, 1, NOW()),
(15, '审核管理', 'forum:audit', 10, 1, '/forum/audit', 'audit', 5, 1, NOW()),
(16, '用户禁言', 'forum:ban', 10, 1, '/forum/ban', 'stop', 6, 1, NOW()),
(17, '标签管理', 'forum:tag', 10, 1, '/forum/tag', 'tag', 7, 1, NOW()),

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
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `icon`, `sort`, `status`, `create_time`) VALUES
-- 用户管理按钮
(100, '用户查询', 'system:user:list', 2, 2, NULL, NULL, 1, 1, NOW()),
(101, '用户新增', 'system:user:add', 2, 2, NULL, NULL, 2, 1, NOW()),
(102, '用户编辑', 'system:user:edit', 2, 2, NULL, NULL, 3, 1, NOW()),
(103, '用户删除', 'system:user:delete', 2, 2, NULL, NULL, 4, 1, NOW()),
(104, '用户导出', 'system:user:export', 2, 2, NULL, NULL, 5, 1, NOW()),
(105, '重置密码', 'system:user:resetPwd', 2, 2, NULL, NULL, 6, 1, NOW()),

-- 角色管理按钮
(110, '角色查询', 'system:role:list', 3, 2, NULL, NULL, 1, 1, NOW()),
(111, '角色新增', 'system:role:add', 3, 2, NULL, NULL, 2, 1, NOW()),
(112, '角色编辑', 'system:role:edit', 3, 2, NULL, NULL, 3, 1, NOW()),
(113, '角色删除', 'system:role:delete', 3, 2, NULL, NULL, 4, 1, NOW()),

-- 版块管理按钮
(120, '版块查询', 'forum:category:list', 11, 2, NULL, NULL, 1, 1, NOW()),
(121, '版块新增', 'forum:category:add', 11, 2, NULL, NULL, 2, 1, NOW()),
(122, '版块编辑', 'forum:category:edit', 11, 2, NULL, NULL, 3, 1, NOW()),
(123, '版块删除', 'forum:category:delete', 11, 2, NULL, NULL, 4, 1, NOW()),

-- 帖子管理按钮
(130, '帖子查询', 'forum:post:list', 12, 2, NULL, NULL, 1, 1, NOW()),
(131, '帖子删除', 'forum:post:delete', 12, 2, NULL, NULL, 2, 1, NOW()),
(132, '帖子置顶', 'forum:post:top', 12, 2, NULL, NULL, 3, 1, NOW()),
(133, '帖子加精', 'forum:post:essence', 12, 2, NULL, NULL, 4, 1, NOW()),
(134, '帖子移动', 'forum:post:move', 12, 2, NULL, NULL, 5, 1, NOW()),
(135, '帖子审核', 'forum:post:audit', 12, 2, NULL, NULL, 6, 1, NOW()),

-- 评论管理按钮
(140, '评论查询', 'forum:comment:list', 13, 2, NULL, NULL, 1, 1, NOW()),
(141, '评论删除', 'forum:comment:delete', 13, 2, NULL, NULL, 2, 1, NOW()),
(142, '评论审核', 'forum:comment:audit', 13, 2, NULL, NULL, 3, 1, NOW()),

-- 举报管理按钮
(150, '举报查询', 'forum:report:list', 14, 2, NULL, NULL, 1, 1, NOW()),
(151, '举报处理', 'forum:report:handle', 14, 2, NULL, NULL, 2, 1, NOW());

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

-- 管理员权限（排除用户管理、角色管理、权限管理的删除权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 2, id, NOW() FROM `sys_permission` WHERE id NOT IN (103, 113);

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

-- ---------------------------------------------------
-- 1.6 数据字典初始数据
-- ---------------------------------------------------
-- 性别字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('sys_gender', '未知', '0', 0, 1, 1, '性别-未知', NOW()),
('sys_gender', '男', '1', 1, 0, 1, '性别-男', NOW()),
('sys_gender', '女', '2', 2, 0, 1, '性别-女', NOW());

-- 用户状态字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('sys_user_status', '禁用', '0', 0, 0, 1, '用户状态-禁用', NOW()),
('sys_user_status', '正常', '1', 1, 1, 1, '用户状态-正常', NOW()),
('sys_user_status', '封禁', '2', 2, 0, 1, '用户状态-封禁', NOW());

-- 是否字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('sys_yes_no', '否', '0', 0, 0, 1, '是否-否', NOW()),
('sys_yes_no', '是', '1', 1, 1, 1, '是否-是', NOW());

-- 审核状态字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('audit_status', '待审核', '0', 0, 0, 1, '审核状态-待审核', NOW()),
('audit_status', '已通过', '1', 1, 1, 1, '审核状态-已通过', NOW()),
('audit_status', '已拒绝', '2', 2, 0, 1, '审核状态-已拒绝', NOW());

-- 帖子类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('post_type', '普通帖', '1', 0, 1, 1, '帖子类型-普通帖', NOW()),
('post_type', '精华帖', '2', 1, 0, 1, '帖子类型-精华帖', NOW()),
('post_type', '置顶帖', '3', 2, 0, 1, '帖子类型-置顶帖', NOW());

-- 内容类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('content_type', '普通文本', '1', 0, 1, 1, '内容类型-普通文本', NOW()),
('content_type', 'Markdown', '2', 1, 0, 1, '内容类型-Markdown', NOW()),
('content_type', '富文本', '3', 2, 0, 1, '内容类型-富文本', NOW());

-- 举报类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('report_type', '垃圾广告', '1', 0, 0, 1, '举报类型-垃圾广告', NOW()),
('report_type', '涉政敏感', '2', 1, 0, 1, '举报类型-涉政敏感', NOW()),
('report_type', '色情低俗', '3', 2, 0, 1, '举报类型-色情低俗', NOW()),
('report_type', '违法违规', '4', 3, 0, 1, '举报类型-违法违规', NOW()),
('report_type', '侵权内容', '5', 4, 0, 1, '举报类型-侵权内容', NOW()),
('report_type', '人身攻击', '6', 5, 0, 1, '举报类型-人身攻击', NOW()),
('report_type', '其他', '7', 6, 1, 1, '举报类型-其他', NOW());

-- 举报处理结果字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('report_result', '无效举报', '1', 0, 0, 1, '举报处理结果-无效举报', NOW()),
('report_result', '警告', '2', 1, 0, 1, '举报处理结果-警告', NOW()),
('report_result', '删除内容', '3', 2, 0, 1, '举报处理结果-删除内容', NOW()),
('report_result', '禁言用户', '4', 3, 0, 1, '举报处理结果-禁言用户', NOW()),
('report_result', '封禁用户', '5', 4, 0, 1, '举报处理结果-封禁用户', NOW());

-- 通知类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('notice_type', '通知', '1', 0, 1, 1, '通知类型-通知', NOW()),
('notice_type', '公告', '2', 1, 0, 1, '通知类型-公告', NOW());

-- 通知级别字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('notice_level', '普通', '1', 0, 1, 1, '通知级别-普通', NOW()),
('notice_level', '重要', '2', 1, 0, 1, '通知级别-重要', NOW()),
('notice_level', '紧急', '3', 2, 0, 1, '通知级别-紧急', NOW());

-- 禁言类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('ban_type', '全站禁言', '1', 0, 0, 1, '禁言类型-全站禁言', NOW()),
('ban_type', '版块禁言', '2', 1, 1, 1, '禁言类型-版块禁言', NOW());

-- 登录方式字典
INSERT INTO `sys_dict` (`dict_type`, `dict_label`, `dict_value`, `sort`, `is_default`, `status`, `remark`, `create_time`) VALUES
('login_type', '密码登录', 'password', 0, 1, 1, '登录方式-密码登录', NOW()),
('login_type', '短信登录', 'sms', 1, 0, 1, '登录方式-短信登录', NOW()),
('login_type', '第三方登录', 'oauth', 2, 0, 1, '登录方式-第三方登录', NOW());

-- ---------------------------------------------------
-- 1.7 系统配置初始数据
-- ---------------------------------------------------
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `description`, `status`, `create_time`) VALUES
-- 基础配置
('网站名称', 'site.name', 'SCampus校园论坛', 'text', '网站名称', 1, NOW()),
('网站描述', 'site.description', '面向校园的交流社区', 'text', '网站SEO描述', 1, NOW()),
('网站关键词', 'site.keywords', '校园论坛,大学生,交流社区', 'text', '网站SEO关键词', 1, NOW()),
('网站备案号', 'site.icp', '', 'text', '网站ICP备案号', 1, NOW()),
('网站版权', 'site.copyright', '© 2024 SCampus', 'text', '网站版权信息', 1, NOW()),

-- 用户配置
('用户默认头像', 'user.default.avatar', '/static/avatar/default.png', 'text', '用户注册时默认头像', 1, NOW()),
('用户初始积分', 'user.init.integral', '0', 'number', '用户注册时初始积分', 1, NOW()),
('用户初始经验', 'user.init.experience', '0', 'number', '用户注册时初始经验', 1, NOW()),
('每日签到积分', 'user.signin.integral', '5', 'number', '每日签到获得积分', 1, NOW()),
('连续签到奖励倍数', 'user.signin.multiplier', '2', 'number', '连续签到奖励倍数', 1, NOW()),

-- 发帖配置
('帖子标题最大长度', 'post.title.maxlength', '200', 'number', '帖子标题最大字符数', 1, NOW()),
('帖子内容最大长度', 'post.content.maxlength', '50000', 'number', '帖子内容最大字符数', 1, NOW()),
('帖子需要审核', 'post.need.audit', 'false', 'boolean', '新帖子是否需要审核', 1, NOW()),
('帖子每日上限', 'post.daily.limit', '50', 'number', '用户每日发帖上限', 1, NOW()),
('帖子附件最大数量', 'post.attachment.maxcount', '9', 'number', '帖子附件最大数量', 1, NOW()),

-- 评论配置
('评论内容最大长度', 'comment.content.maxlength', '1000', 'number', '评论内容最大字符数', 1, NOW()),
('评论需要审核', 'comment.need.audit', 'false', 'boolean', '新评论是否需要审核', 1, NOW()),
('评论每日上限', 'comment.daily.limit', '200', 'number', '用户每日评论上限', 1, NOW()),

-- 文件上传配置
('文件最大大小', 'file.max.size', '10485760', 'number', '文件上传最大大小(字节)', 1, NOW()),
('图片最大大小', 'image.max.size', '5242880', 'number', '图片上传最大大小(字节)', 1, NOW()),
('允许图片类型', 'image.allow.types', 'jpg,jpeg,png,gif,bmp,webp', 'text', '允许上传的图片类型', 1, NOW()),
('允许文件类型', 'file.allow.types', 'doc,docx,pdf,txt,xls,xlsx,ppt,pptx,zip,rar', 'text', '允许上传的文件类型', 1, NOW()),
('存储方式', 'file.storage.type', 'local', 'text', '文件存储方式：local/oss/cos', 1, NOW()),

-- 敏感词配置
('敏感词过滤开关', 'sensitive.filter.enabled', 'true', 'boolean', '是否开启敏感词过滤', 1, NOW()),
('敏感词替换字符', 'sensitive.replace.char', '*', 'text', '敏感词替换字符', 1, NOW()),

-- 邮件配置
('SMTP服务器', 'mail.smtp.host', '', 'text', 'SMTP服务器地址', 1, NOW()),
('SMTP端口', 'mail.smtp.port', '465', 'number', 'SMTP服务器端口', 1, NOW()),
('发件人邮箱', 'mail.from.email', '', 'text', '发件人邮箱地址', 1, NOW()),
('发件人名称', 'mail.from.name', 'SCampus', 'text', '发件人显示名称', 1, NOW()),

-- OSS配置
('OSS Endpoint', 'oss.endpoint', '', 'text', '阿里云OSS Endpoint', 1, NOW()),
('OSS Bucket', 'oss.bucket', '', 'text', '阿里云OSS Bucket名称', 1, NOW()),
('OSS 域名', 'oss.domain', '', 'text', '阿里云OSS访问域名', 1, NOW());

-- ---------------------------------------------------
-- 1.8 默认敏感词
-- ---------------------------------------------------
INSERT INTO `sys_sensitive_word` (`word`, `word_type`, `replace_word`, `status`, `create_time`) VALUES
('色情', 1, '**', 1, NOW()),
('暴力', 1, '**', 1, NOW()),
('反动', 1, '**', 1, NOW()),
('法轮功', 1, '***', 1, NOW()),
('赌博', 2, '**', 1, NOW()),
('诈骗', 2, '**', 1, NOW()),
('发票', 2, '**', 1, NOW()),
('代开发票', 1, '****', 1, NOW()),
('兼职刷单', 1, '****', 1, NOW()),
('贷款', 3, '**', 1, NOW());


-- =====================================================
-- 2. 版块服务数据 (forum_category_db)
-- 包含: 版块分类、版块、版主关联
-- =====================================================

USE `forum_category_db`;

-- ---------------------------------------------------
-- 2.1 版块分类
-- ---------------------------------------------------
INSERT INTO `forum_category` (`id`, `category_name`, `category_icon`, `category_desc`, `sort`, `status`, `create_time`) VALUES
(1, '校园生活', 'home', '校园日常、学习生活相关话题', 1, 1, NOW()),
(2, '学习交流', 'book', '学习资料、课程讨论、学术交流', 2, 1, NOW()),
(3, '社团活动', 'team', '社团招新、活动通知、社团风采', 3, 1, NOW()),
(4, '二手市场', 'shopping', '二手物品交易、求购信息', 4, 1, NOW()),
(5, '求职招聘', 'solution', '实习招聘、求职经验分享', 5, 1, NOW()),
(6, '休闲娱乐', 'coffee', '游戏、电影、音乐等娱乐话题', 6, 1, NOW()),
(7, '失物招领', 'search', '丢失物品、捡到物品发布', 7, 1, NOW()),
(8, '校园公告', 'notification', '学校通知、重要公告', 8, 1, NOW());

-- ---------------------------------------------------
-- 2.2 版块
-- ---------------------------------------------------
INSERT INTO `forum_forum` (`id`, `category_id`, `forum_name`, `forum_icon`, `forum_desc`, `forum_rules`, `sort`, `status`, `create_time`) VALUES
-- 校园生活
(1, 1, '校园新鲜事', 'bulb', '分享校园里的新鲜事、有趣的事', '1. 文明发帖，禁止人身攻击\n2. 内容真实，不造谣传谣\n3. 保护隐私，不泄露他人信息', 1, 1, NOW()),
(2, 1, '美食推荐', 'crown', '校园美食、周边美食推荐', '1. 推荐内容需真实\n2. 禁止商业广告\n3. 分享真实用餐体验', 2, 1, NOW()),
(3, 1, '宿舍生活', 'home', '宿舍日常、室友相处经验分享', '1. 尊重他人隐私\n2. 不发表地域歧视言论\n3. 文明交流', 3, 1, NOW()),

-- 学习交流
(4, 2, '课程讨论', 'book', '各专业课程学习心得、资料分享', '1. 禁止发布考试答案\n2. 鼓励学术讨论\n3. 资料分享注意版权', 1, 1, NOW()),
(5, 2, '考研专区', 'rocket', '考研经验分享、资料交流', '1. 分享真实经验\n2. 禁止贩卖资料\n3. 互帮互助', 2, 1, NOW()),
(6, 2, '考试资讯', 'notification', '各类考试通知、报名时间', '1. 信息准确可靠\n2. 及时更新\n3. 禁止虚假信息', 3, 1, NOW()),

-- 社团活动
(7, 3, '社团招新', 'team', '社团招新公告、新人交流', '1. 内容真实\n2. 禁止虚假宣传\n3. 文明招新', 1, 1, NOW()),
(8, 3, '活动预告', 'calendar', '校园活动预告、回顾', '1. 活动信息准确\n2. 图片内容健康\n3. 鼓励原创分享', 2, 1, NOW()),

-- 二手市场
(9, 4, '闲置交易', 'shopping', '二手物品买卖、赠送', '1. 禁止违禁物品交易\n2. 交易需诚信\n3. 线下交易注意安全', 1, 1, NOW()),
(10, 4, '求购专区', 'search', '求购物品发布', '1. 描述清楚需求\n2. 价格合理\n3. 联系方式准确', 2, 1, NOW()),

-- 求职招聘
(11, 5, '实习招聘', 'solution', '实习岗位推荐、求职经验', '1. 信息真实有效\n2. 禁止虚假招聘\n3. 保护个人隐私', 1, 1, NOW()),
(12, 5, '求职经验', 'crown', '面试经验、职场心得分享', '1. 分享真实经验\n2. 鼓励互助\n3. 内容健康积极', 2, 1, NOW()),

-- 休闲娱乐
(13, 6, '游戏交流', 'game', '游戏讨论、组队开黑', '1. 文明讨论\n2. 禁止外挂推荐\n3. 理性游戏', 1, 1, NOW()),
(14, 6, '影音娱乐', 'video', '电影、电视剧、音乐推荐', '1. 内容健康\n2. 尊重版权\n3. 分享观后感', 2, 1, NOW()),
(15, 6, '体育健身', 'trophy', '运动健身、体育赛事讨论', '1. 鼓励健康生活\n2. 文明讨论比赛\n3. 分享健身经验', 3, 1, NOW()),

-- 失物招领
(16, 7, '寻物启事', 'search', '丢失物品发布', '1. 描述清楚物品特征\n2. 留下联系方式\n3. 找到后请及时更新状态', 1, 1, NOW()),
(17, 7, '失物招领', 'gift', '捡到物品发布', '1. 描述清楚物品特征\n2. 保护失主隐私\n3. 诚信归还', 2, 1, NOW()),

-- 校园公告
(18, 8, '学校通知', 'notification', '学校官方通知公告', '1. 内容以官方为准\n2. 禁止擅自发布\n3. 及时关注更新', 1, 1, NOW()),
(19, 8, '论坛公告', 'notification', '论坛运营相关公告', '1. 重要公告置顶\n2. 规则说明\n3. 活动通知', 2, 1, NOW());

-- ---------------------------------------------------
-- 2.3 版主关联
-- ---------------------------------------------------
INSERT INTO `forum_moderator` (`forum_id`, `user_id`, `moderator_type`, `create_time`) VALUES
(1, 3, 1, NOW()),
(4, 3, 1, NOW()),
(9, 3, 2, NOW());


-- =====================================================
-- 3. 帖子服务数据 (forum_post_db)
-- 包含: 标签
-- =====================================================

USE `forum_post_db`;

-- ---------------------------------------------------
-- 3.1 默认标签
-- ---------------------------------------------------
INSERT INTO `forum_tag` (`tag_name`, `tag_color`, `use_count`, `status`, `create_time`) VALUES
('学习', '#1890ff', 0, 1, NOW()),
('生活', '#52c41a', 0, 1, NOW()),
('美食', '#fa8c16', 0, 1, NOW()),
('游戏', '#722ed1', 0, 1, NOW()),
('电影', '#eb2f96', 0, 1, NOW()),
('音乐', '#13c2c2', 0, 1, NOW()),
('运动', '#faad14', 0, 1, NOW()),
('考研', '#f5222d', 0, 1, NOW()),
('求职', '#2f54eb', 0, 1, NOW()),
('求助', '#fa541c', 0, 1, NOW());


-- =====================================================
-- 4. 通知服务数据 (forum_notify_db)
-- 包含: 通知公告
-- =====================================================

USE `forum_notify_db`;

-- ---------------------------------------------------
-- 4.1 默认公告
-- ---------------------------------------------------
INSERT INTO `sys_notice` (`notice_title`, `notice_content`, `notice_type`, `notice_level`, `target_type`, `sender_id`, `is_top`, `publish_time`, `status`, `create_time`) VALUES
('欢迎来到SCampus校园论坛', '<p>亲爱的同学们，欢迎来到SCampus校园论坛！</p><p>这是一个属于我们校园的交流平台，在这里你可以：</p><ul><li>分享校园生活点滴</li><li>交流学习经验心得</li><li>参与社团活动讨论</li><li>发布二手物品交易</li><li>获取求职招聘信息</li></ul><p>请遵守论坛规则，文明发言，共同维护良好的社区环境。</p>', 2, 2, 1, 1, 1, NOW(), 1, NOW()),
('论坛使用指南', '<h2>论坛使用指南</h2><h3>1. 如何发帖</h3><p>登录后点击页面右上角的"发布"按钮，选择对应版块，填写标题和内容即可发帖。</p><h3>2. 如何评论</h3><p>在帖子详情页面下方输入评论内容，点击"发表评论"即可。</p><h3>3. 如何收藏</h3><p>点击帖子右上角的收藏按钮，即可将帖子添加到收藏夹。</p><h3>4. 如何举报</h3><p>如发现违规内容，请点击帖子右上角的"举报"按钮进行举报。</p>', 1, 1, 1, 1, 0, NOW(), 1, NOW());


-- =====================================================
-- 初始化数据完成
-- =====================================================
