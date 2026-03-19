package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录VO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "审核记录VO")
public class ApproveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "内容类型: 1-帖子 2-评论 3-头像 4-昵称")
    private Integer contentType;

    @Schema(description = "内容类型名称")
    private String contentTypeName;

    @Schema(description = "内容ID")
    private Long contentId;

    @Schema(description = "内容标题")
    private String title;

    @Schema(description = "内容摘要")
    private String content;

    @Schema(description = "审核状态: 0-待审核 1-审核通过 2-审核拒绝")
    private Integer status;

    @Schema(description = "审核状态名称")
    private String statusName;

    @Schema(description = "审核人ID")
    private Long auditorId;

    @Schema(description = "审核人昵称")
    private String auditorName;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @Schema(description = "敏感词列表")
    private String sensitiveWords;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
