package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户禁言VO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户禁言VO")
public class UserBanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "禁言ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "禁言类型: 1-全站禁言 2-板块禁言")
    private Integer banType;

    @Schema(description = "禁言类型名称")
    private String banTypeName;

    @Schema(description = "板块ID")
    private Long forumId;

    @Schema(description = "板块名称")
    private String forumName;

    @Schema(description = "禁言原因")
    private String reason;

    @Schema(description = "关联举报ID")
    private Long reportId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人昵称")
    private String operatorName;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "禁言状态: 0-已解除 1-禁言中 2-已过期")
    private Integer status;

    @Schema(description = "禁言状态名称")
    private String statusName;

    @Schema(description = "剩余禁言时间（小时）")
    private Long remainingHours;

    @Schema(description = "解除时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;

    @Schema(description = "解除操作人昵称")
    private String releaseOperatorName;

    @Schema(description = "解除原因")
    private String releaseReason;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
