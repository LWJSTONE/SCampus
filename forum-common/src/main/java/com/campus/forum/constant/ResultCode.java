package com.campus.forum.constant;

import lombok.Getter;

/**
 * 响应码枚举类
 * 定义系统中使用的各种响应码
 *
 * @author campus
 * @since 2024-01-01
 */
@Getter
public enum ResultCode {

    // ===================== 成功响应码 =====================

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 创建成功
     */
    CREATED(201, "创建成功"),

    /**
     * 更新成功
     */
    UPDATED(200, "更新成功"),

    /**
     * 删除成功
     */
    DELETED(200, "删除成功"),

    // ===================== 客户端错误码 4xx =====================

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 参数校验失败
     */
    PARAM_VALID_ERROR(400, "参数校验失败"),

    /**
     * 参数绑定失败
     */
    PARAM_BIND_ERROR(400, "参数绑定失败"),

    /**
     * 参数缺失
     */
    PARAM_MISSING_ERROR(400, "参数缺失"),

    /**
     * 参数类型错误
     */
    PARAM_TYPE_ERROR(400, "参数类型错误"),

    /**
     * 请求体格式错误
     */
    REQUEST_BODY_ERROR(400, "请求体格式错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * Token无效
     */
    TOKEN_INVALID(401, "Token无效，请重新登录"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(401, "Token已过期，请重新登录"),

    /**
     * 无权限访问
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求数据冲突
     */
    CONFLICT(409, "数据冲突"),

    /**
     * 文件上传大小超限
     */
    FILE_SIZE_EXCEEDED(413, "上传文件大小超过限制"),

    /**
     * 请求频率过高
     */
    TOO_MANY_REQUESTS(429, "请求频率过高，请稍后再试"),

    // ===================== 服务端错误码 5xx =====================

    /**
     * 系统内部错误
     */
    SYSTEM_ERROR(500, "系统内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    /**
     * 网关超时
     */
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ===================== 业务错误码 1xxx =====================

    /**
     * 业务异常
     */
    BUSINESS_ERROR(1000, "业务处理失败"),

    /**
     * 数据已存在
     */
    DATA_EXISTS(1001, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(1002, "数据不存在"),

    /**
     * 数据操作失败
     */
    DATA_OPERATION_ERROR(1003, "数据操作失败"),

    // ===================== 用户相关错误码 2xxx =====================

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(2001, "用户不存在"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(2002, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(2003, "邮箱已被注册"),

    /**
     * 手机号已存在
     */
    PHONE_EXISTS(2004, "手机号已被注册"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(2005, "密码错误"),

    /**
     * 用户被禁用
     */
    USER_DISABLED(2006, "用户已被禁用"),

    /**
     * 用户未激活
     */
    USER_INACTIVE(2007, "用户未激活"),

    /**
     * 原密码错误
     */
    OLD_PASSWORD_ERROR(2008, "原密码错误"),

    /**
     * 两次密码不一致
     */
    PASSWORD_NOT_MATCH(2009, "两次密码不一致"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(2010, "验证码错误"),

    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(2011, "验证码已过期"),

    /**
     * 账号已登录
     */
    ACCOUNT_LOGGED(2012, "账号已在其他地方登录"),

    // ===================== 帖子相关错误码 3xxx =====================

    /**
     * 帖子不存在
     */
    POST_NOT_FOUND(3001, "帖子不存在"),

    /**
     * 帖子已删除
     */
    POST_DELETED(3002, "帖子已被删除"),

    /**
     * 帖子已下架
     */
    POST_OFFLINE(3003, "帖子已下架"),

    /**
     * 无权操作帖子
     */
    POST_NO_PERMISSION(3004, "无权操作此帖子"),

    /**
     * 帖子标题已存在
     */
    POST_TITLE_EXISTS(3005, "帖子标题已存在"),

    // ===================== 评论相关错误码 4xxx =====================

    /**
     * 评论不存在
     */
    COMMENT_NOT_FOUND(4001, "评论不存在"),

    /**
     * 评论已删除
     */
    COMMENT_DELETED(4002, "评论已被删除"),

    /**
     * 无权操作评论
     */
    COMMENT_NO_PERMISSION(4003, "无权操作此评论"),

    /**
     * 评论内容过长
     */
    COMMENT_TOO_LONG(4004, "评论内容过长"),

    // ===================== 文件相关错误码 5xxx =====================

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORT(5002, "文件类型不支持"),

    /**
     * 文件不存在
     */
    FILE_NOT_FOUND(5003, "文件不存在"),

    /**
     * 文件删除失败
     */
    FILE_DELETE_ERROR(5004, "文件删除失败"),

    // ===================== 权限相关错误码 6xxx =====================

    /**
     * 角色不存在
     */
    ROLE_NOT_FOUND(6001, "角色不存在"),

    /**
     * 角色编码已存在
     */
    ROLE_CODE_EXISTS(6002, "角色编码已存在"),

    /**
     * 权限不存在
     */
    PERMISSION_NOT_FOUND(6003, "权限不存在"),

    /**
     * 权限编码已存在
     */
    PERMISSION_CODE_EXISTS(6004, "权限编码已存在"),

    /**
     * 角色已分配用户
     */
    ROLE_HAS_USER(6005, "角色已分配给用户，无法删除"),

    // ===================== 第三方服务错误码 7xxx =====================

    /**
     * 短信发送失败
     */
    SMS_SEND_ERROR(7001, "短信发送失败"),

    /**
     * 邮件发送失败
     */
    EMAIL_SEND_ERROR(7002, "邮件发送失败"),

    /**
     * Redis连接失败
     */
    REDIS_CONNECT_ERROR(7003, "Redis连接失败"),

    /**
     * 数据库操作失败
     */
    DATABASE_ERROR(7004, "数据库操作失败");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 响应码
     * @return 枚举
     */
    public static ResultCode getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
}
