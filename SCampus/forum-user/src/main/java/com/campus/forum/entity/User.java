package com.campus.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.forum.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 
 * 存储用户基本信息，包括：
 * - 账户信息（用户名、密码、邮箱、手机号）
 * - 个人信息（昵称、头像、性别、简介）
 * - 学校信息（学校ID、学号、专业、年级）
 * - 状态信息（状态、最后登录时间、最后登录IP）
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 昵称（显示名称）
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
    private java.time.LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 帖子数量（冗余字段，提高查询性能）
     */
    private Integer postCount;

    /**
     * 评论数量（冗余字段，提高查询性能）
     */
    private Integer commentCount;

    /**
     * 粉丝数量（冗余字段，提高查询性能）
     */
    private Integer followerCount;

    /**
     * 关注数量（冗余字段，提高查询性能）
     */
    private Integer followingCount;

    /**
     * 收藏数量（冗余字段，提高查询性能）
     */
    private Integer collectionCount;
}
