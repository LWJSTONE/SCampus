package com.campus.forum.dto;

import com.campus.forum.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核查询DTO
 *
 * @author campus
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审核查询DTO")
public class ApproveQueryDTO extends PageQueryDTO {

    @Schema(description = "审核状态: 0-待审核 1-审核通过 2-审核拒绝")
    private Integer status;

    @Schema(description = "内容类型: 1-帖子 2-评论 3-头像 4-昵称")
    private Integer contentType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
