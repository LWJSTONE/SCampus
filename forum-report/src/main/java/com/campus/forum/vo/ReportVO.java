package com.campus.forum.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报VO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "举报VO")
public class ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "举报ID")
    private Long id;

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "举报人昵称")
    private String reporterName;

    @Schema(description = "举报人头像")
    private String reporterAvatar;

    @Schema(description = "被举报用户ID")
    private Long reportedUserId;

    @Schema(description = "被举报用户昵称")
    private String reportedUserName;

    @Schema(description = "举报类型: 1-帖子 2-评论 3-用户")
    private Integer reportType;

    @Schema(description = "举报类型名称")
    private String reportTypeName;

    @Schema(description = "被举报内容ID")
    private Long targetId;

    @Schema(description = "举报原因类型")
    private Integer reasonType;

    @Schema(description = "举报原因类型名称")
    private String reasonTypeName;

    @Schema(description = "举报原因详情")
    private String reason;

    @Schema(description = "举报截图")
    private String images;

    @Schema(description = "处理状态: 0-待处理 1-处理中 2-已处理 3-已驳回")
    private Integer status;

    @Schema(description = "处理状态名称")
    private String statusName;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人昵称")
    private String handlerName;

    @Schema(description = "处理结果")
    private Integer result;

    @Schema(description = "处理结果名称")
    private String resultName;

    @Schema(description = "处理备注")
    private String remark;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
