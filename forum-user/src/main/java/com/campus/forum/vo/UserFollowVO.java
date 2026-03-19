package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注VO
 * 
 * 用于返回关注列表和粉丝列表中的用户信息，包含：
 * - 用户基本信息
 * - 关注时间
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户关注VO")
public class UserFollowVO implements Serializable {

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
     * 关注时间
     */
    @Schema(description = "关注时间")
    private LocalDateTime followTime;
}
