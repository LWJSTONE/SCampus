package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 审核处理DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "审核处理DTO")
public class ApproveHandleDTO {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态: 1-审核通过 2-审核拒绝", required = true)
    private Integer status;

    @Schema(description = "审核意见")
    private String auditRemark;
}
