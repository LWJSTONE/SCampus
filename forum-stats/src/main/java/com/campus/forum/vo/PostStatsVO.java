package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子统计VO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "帖子统计")
public class PostStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总帖子数")
    private Long totalPosts;

    @Schema(description = "今日新增帖子")
    private Integer todayNewPosts;

    @Schema(description = "本周新增帖子")
    private Integer weekNewPosts;

    @Schema(description = "本月新增帖子")
    private Integer monthNewPosts;

    @Schema(description = "待审核帖子")
    private Integer pendingPosts;

    @Schema(description = "已发布帖子")
    private Integer publishedPosts;

    @Schema(description = "已删除帖子")
    private Integer deletedPosts;

    @Schema(description = "平均每帖评论数")
    private Double avgCommentsPerPost;

    @Schema(description = "平均每帖点赞数")
    private Double avgLikesPerPost;

    @Schema(description = "总评论数")
    private Long totalComments;

    @Schema(description = "今日新增评论")
    private Integer todayNewComments;

    @Schema(description = "帖子增长率(%)")
    private Double growthRate;
}
