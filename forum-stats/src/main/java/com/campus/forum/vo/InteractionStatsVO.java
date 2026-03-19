package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 互动统计VO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "互动统计")
public class InteractionStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总点赞数")
    private Long totalLikes;

    @Schema(description = "今日点赞数")
    private Integer todayLikes;

    @Schema(description = "总评论数")
    private Long totalComments;

    @Schema(description = "今日评论数")
    private Integer todayComments;

    @Schema(description = "总收藏数")
    private Long totalCollects;

    @Schema(description = "今日收藏数")
    private Integer todayCollects;

    @Schema(description = "总关注数")
    private Long totalFollows;

    @Schema(description = "今日关注数")
    private Integer todayFollows;

    @Schema(description = "总浏览量")
    private Long totalViews;

    @Schema(description = "今日浏览量")
    private Long todayViews;

    @Schema(description = "互动增长率(%)")
    private Double growthRate;
}
