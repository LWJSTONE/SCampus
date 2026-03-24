package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 举报创建DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "举报创建DTO")
public class ReportCreateDTO {

    @NotNull(message = "被举报用户ID不能为空")
    @Schema(description = "被举报用户ID", required = true)
    private Long reportedUserId;

    @NotNull(message = "举报类型不能为空")
    @Schema(description = "举报类型: 1-帖子 2-评论 3-用户", required = true)
    private Integer reportType;

    @NotNull(message = "被举报内容ID不能为空")
    @Schema(description = "被举报内容ID", required = true)
    private Long targetId;

    @NotNull(message = "举报原因类型不能为空")
    @Schema(description = "举报原因类型: 1-垃圾广告 2-色情低俗 3-违法违规 4-人身攻击 5-恶意灌水 6-其他", required = true)
    private Integer reasonType;

    @Schema(description = "举报原因详情")
    private String reason;

    @Schema(description = "举报截图URL列表")
    private String images;
}
