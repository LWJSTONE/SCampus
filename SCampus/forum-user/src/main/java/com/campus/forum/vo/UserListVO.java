package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户列表VO
 * 
 * 用于返回用户列表中的用户基本信息，包含：
 * - 基本信息（ID、用户名、昵称、头像）
 * - 统计信息（帖子数、粉丝数等）
 * - 状态信息
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户列表VO")
public class UserListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;

    /**
     * 性别描述
     */
    @Schema(description = "性别描述")
    private String genderDesc;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介")
    private String bio;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 年级
     */
    @Schema(description = "年级")
    private String grade;

    /**
     * 状态（0-禁用，1-正常）
     */
    @Schema(description = "状态（0-禁用，1-正常）")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 帖子数量
     */
    @Schema(description = "帖子数量")
    private Integer postCount;

    /**
     * 粉丝数量
     */
    @Schema(description = "粉丝数量")
    private Integer followerCount;

    /**
     * 关注数量
     */
    @Schema(description = "关注数量")
    private Integer followingCount;

    /**
     * 是否已关注（当前用户是否关注了该用户）
     */
    @Schema(description = "是否已关注")
    private Boolean followed;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
