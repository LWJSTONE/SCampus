package com.campus.forum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 统计查询DTO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "统计查询参数")
public class StatsQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "时间范围类型：day-今日，week-本周，month-本月，custom-自定义")
    private String rangeType;

    @Schema(description = "统计类型：user-用户，post-帖子，interaction-互动，overview-概览")
    private String statsType;
}
