package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 认证用户实体类
 * 
 * <p>用于认证服务的用户实体，包含用户的基本信息和认证相关字段</p>
 * <p>对应数据库表：sys_user</p>
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class AuthUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 学校ID
     */
    private Long schoolId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private String grade;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 账户锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 密码修改时间
     */
    private LocalDateTime passwordUpdateTime;

    /**
     * 邮箱验证状态（0-未验证，1-已验证）
     */
    private Integer emailVerified;

    /**
     * 手机验证状态（0-未验证，1-已验证）
     */
    private Integer phoneVerified;
}
