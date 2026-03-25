package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 举报处理DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "举报处理DTO")
public class ReportHandleDTO {

    @NotNull(message = "处理结果不能为空")
    @Schema(description = "处理结果: 0-无违规 1-警告 2-删除内容 3-禁言 4-封号", required = true)
    private Integer result;

    @Schema(description = "处理备注")
    private String remark;

    @Schema(description = "禁言天数（处理结果为禁言时使用）")
    private Integer banDays;
}
