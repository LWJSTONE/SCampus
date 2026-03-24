package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户基本信息VO
 *
 * 用于跨服务传输用户基本信息，包含：
 * - 用户ID
 * - 用户名
 * - 昵称
 * - 头像URL
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户基本信息VO")
public class UserVO implements Serializable {

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
     * 状态（0-禁用，1-正常）
     */
    @Schema(description = "状态（0-禁用，1-正常）")
    private Integer status;
}
