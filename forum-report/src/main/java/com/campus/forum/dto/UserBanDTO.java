package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户禁言DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户禁言DTO")
public class UserBanDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long userId;

    @Schema(description = "禁言类型: 1-全站禁言 2-板块禁言")
    private Integer banType;

    @Schema(description = "板块ID（板块禁言时使用）")
    private Long forumId;

    @NotNull(message = "禁言原因不能为空")
    @Schema(description = "禁言原因", required = true)
    private String reason;

    @Schema(description = "关联举报ID")
    private Long reportId;

    @NotNull(message = "禁言天数不能为空")
    @Schema(description = "禁言天数", required = true)
    private Integer banDays;
}
