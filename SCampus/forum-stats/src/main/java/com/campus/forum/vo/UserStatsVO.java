package com.campus.forum.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户统计VO
 * 
 * @author campus
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户统计")
public class UserStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "今日新增用户")
    private Integer todayNewUsers;

    @Schema(description = "本周新增用户")
    private Integer weekNewUsers;

    @Schema(description = "本月新增用户")
    private Integer monthNewUsers;

    @Schema(description = "今日活跃用户")
    private Integer todayActiveUsers;

    @Schema(description = "本周活跃用户")
    private Integer weekActiveUsers;

    @Schema(description = "本月活跃用户")
    private Integer monthActiveUsers;

    @Schema(description = "用户增长率(%)")
    private Double growthRate;

    @Schema(description = "活跃率(%)")
    private Double activeRate;
}
