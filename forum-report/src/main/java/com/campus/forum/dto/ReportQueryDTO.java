package com.campus.forum.dto;

import com.campus.forum.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 举报查询DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "举报查询DTO")
public class ReportQueryDTO extends PageQueryDTO {

    @Schema(description = "处理状态: 0-待处理 1-处理中 2-已处理 3-已驳回")
    private Integer status;

    @Schema(description = "举报类型: 1-帖子 2-评论 3-用户")
    private Integer reportType;

    @Schema(description = "举报原因类型: 1-垃圾广告 2-色情低俗 3-违法违规 4-人身攻击 5-恶意灌水 6-其他")
    private Integer reasonType;

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "被举报用户ID")
    private Long reportedUserId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
