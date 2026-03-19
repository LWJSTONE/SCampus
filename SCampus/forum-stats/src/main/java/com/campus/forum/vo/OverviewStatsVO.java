package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统概览统计VO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "系统概览统计")
public class OverviewStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "今日新增用户")
    private Integer todayNewUsers;

    @Schema(description = "总帖子数")
    private Long totalPosts;

    @Schema(description = "今日新增帖子")
    private Integer todayNewPosts;

    @Schema(description = "总评论数")
    private Long totalComments;

    @Schema(description = "今日新增评论")
    private Integer todayNewComments;

    @Schema(description = "今日活跃用户")
    private Integer todayActiveUsers;

    @Schema(description = "总浏览量")
    private Long totalViews;

    @Schema(description = "今日浏览量")
    private Long todayViews;

    @Schema(description = "帖子增长率(%)")
    private Double postGrowthRate;

    @Schema(description = "用户增长率(%)")
    private Double userGrowthRate;
}
