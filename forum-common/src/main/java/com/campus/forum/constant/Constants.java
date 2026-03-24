package com.campus.forum.constant;

/**
 * 系统常量类
 * 定义系统中使用的各种常量
 *
 * @author campus
 * @since 2024-01-01
 */
public class Constants {

    private Constants() {
        // 私有构造方法，防止实例化
    }

    // ===================== 系统相关 =====================

    /**
     * 系统名称
     */
    public static final String SYSTEM_NAME = "校园论坛系统";

    /**
     * 系统版本
     */
    public static final String SYSTEM_VERSION = "1.0.0";

    /**
     * 字符编码
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    // ===================== 分页相关 =====================

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    // ===================== 文件上传相关 =====================

    /**
     * 上传路径
     */
    public static final String UPLOAD_PATH = "/upload/";

    /**
     * 上传目录
     */
    public static final String UPLOAD_DIR = "/data/upload/";

    /**
     * 最大文件大小（字节）
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    /**
     * 图片文件扩展名
     */
    public static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

    /**
     * 文档文件扩展名
     */
    public static final String[] DOC_EXTENSIONS = {"doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx"};

    // ===================== Redis Key前缀 =====================

    /**
     * 缓存Key前缀
     */
    public static final String CACHE_PREFIX = "campus:forum:";

    /**
     * 用户Token Key前缀
     */
    public static final String TOKEN_PREFIX = CACHE_PREFIX + "token:";

    /**
     * 验证码Key前缀
     */
    public static final String CAPTCHA_PREFIX = CACHE_PREFIX + "captcha:";

    /**
     * 用户信息Key前缀
     */
    public static final String USER_PREFIX = CACHE_PREFIX + "user:";

    /**
     * 角色权限Key前缀
     */
    public static final String ROLE_PERMISSION_PREFIX = CACHE_PREFIX + "role:permission:";

    /**
     * 在线用户Key前缀
     */
    public static final String ONLINE_USER_PREFIX = CACHE_PREFIX + "online:user:";

    // ===================== Token相关 =====================

    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX_BEARER = "Bearer ";

    /**
     * Token过期时间（秒）- 24小时
     */
    public static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60L;

    /**
     * 刷新Token过期时间（秒）- 7天
     */
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60L;

    // ===================== 验证码相关 =====================

    /**
     * 验证码过期时间（秒）- 5分钟
     */
    public static final long CAPTCHA_EXPIRE_TIME = 5 * 60L;

    /**
     * 验证码长度
     */
    public static final int CAPTCHA_LENGTH = 4;

    // ===================== 状态相关 =====================

    /**
     * 状态 - 禁用
     */
    public static final Integer STATUS_DISABLE = 0;

    /**
     * 状态 - 正常
     */
    public static final Integer STATUS_ENABLE = 1;

    /**
     * 删除标志 - 未删除
     */
    public static final Integer DELETE_FLAG_NO = 0;

    /**
     * 删除标志 - 已删除
     */
    public static final Integer DELETE_FLAG_YES = 1;

    // ===================== 性别相关 =====================

    /**
     * 性别 - 未知
     */
    public static final Integer GENDER_UNKNOWN = 0;

    /**
     * 性别 - 男
     */
    public static final Integer GENDER_MALE = 1;

    /**
     * 性别 - 女
     */
    public static final Integer GENDER_FEMALE = 2;

    // ===================== 权限类型 =====================

    /**
     * 权限类型 - 菜单
     */
    public static final Integer PERMISSION_TYPE_MENU = 1;

    /**
     * 权限类型 - 按钮
     */
    public static final Integer PERMISSION_TYPE_BUTTON = 2;

    /**
     * 权限类型 - 接口
     */
    public static final Integer PERMISSION_TYPE_API = 3;

    // ===================== 角色编码 =====================

    /**
     * 超级管理员角色编码
     */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    /**
     * 管理员角色编码
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 普通用户角色编码
     */
    public static final String ROLE_USER = "USER";

    // ===================== 默认值 =====================

    /**
     * 【安全修复】已移除硬编码默认密码
     *
     * 原安全问题：硬编码的弱密码 "123456" 可被攻击者利用
     * 修复方案：
     * 1. 移除 DEFAULT_PASSWORD 常量
     * 2. 使用 PasswordUtils.generateRandomPassword() 生成随机初始密码
     * 3. 要求用户首次登录时强制修改密码
     *
     * 初始化用户密码的正确方式：
     * - 开发/测试环境：使用随机生成的临时密码
     * - 生产环境：通过配置文件设置或发送邮件/短信邀请用户设置密码
     *
     * @see com.campus.forum.utils.PasswordUtils#generateRandomPassword(int)
     */
    // 已移除 DEFAULT_PASSWORD = "123456" - 禁止使用弱默认密码

    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "/static/images/default-avatar.png";

    /**
     * 默认页数
     */
    public static final String DEFAULT_SORT_ORDER = "desc";

    // ===================== 正则表达式 =====================

    /**
     * 手机号正则
     */
    public static final String REGEX_MOBILE = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则
     */
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * 用户名正则（字母开头，允许字母数字下划线，3-20位）
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";

    /**
     * 密码正则（6-20位，至少包含字母和数字）
     */
    public static final String REGEX_PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$";

    // ===================== 时间格式 =====================

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // ===================== 帖子状态 =====================

    /**
     * 帖子状态 - 草稿
     */
    public static final Integer POST_STATUS_DRAFT = 0;

    /**
     * 帖子状态 - 已发布
     */
    public static final Integer POST_STATUS_PUBLISHED = 1;

    /**
     * 帖子状态 - 已下架
     */
    public static final Integer POST_STATUS_OFFLINE = 2;

    // ===================== 评论状态 =====================

    /**
     * 评论状态 - 待审核
     */
    public static final Integer COMMENT_STATUS_PENDING = 0;

    /**
     * 评论状态 - 已通过
     */
    public static final Integer COMMENT_STATUS_APPROVED = 1;

    /**
     * 评论状态 - 已拒绝
     */
    public static final Integer COMMENT_STATUS_REJECTED = 2;
}
