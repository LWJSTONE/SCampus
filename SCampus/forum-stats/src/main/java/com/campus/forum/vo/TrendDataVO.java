package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 趋势数据VO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "趋势数据")
public class TrendDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "用户增长趋势")
    private List<Integer> userTrend;

    @Schema(description = "帖子增长趋势")
    private List<Integer> postTrend;

    @Schema(description = "评论增长趋势")
    private List<Integer> commentTrend;

    @Schema(description = "浏览量趋势")
    private List<Long> viewTrend;

    @Schema(description = "活跃用户趋势")
    private List<Integer> activeUserTrend;
}
