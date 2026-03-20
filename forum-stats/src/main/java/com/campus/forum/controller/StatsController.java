package com.campus.forum.controller;

import com.campus.forum.entity.Result;
import com.campus.forum.service.StatsService;
import com.campus.forum.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 统计控制器
 * 
 * 提供系统统计数据接口，包括：
 * - 系统概览统计
 * - 用户统计
 * - 帖子统计
 * - 互动统计
 * - 趋势数据
 * - 报表导出
 * 
 * @author campus
 * @since 2024-01-01
 */
@Tag(name = "统计接口", description = "系统统计数据相关接口")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取系统概览统计
     */
    @Operation(summary = "获取系统概览统计", description = "获取系统整体数据概览，包括用户数、帖子数、评论数等")
    @GetMapping("/overview")
    public Result<OverviewStatsVO> getOverview() {
        return Result.success(statsService.getOverview());
    }

    /**
     * 获取用户统计
     */
    @Operation(summary = "获取用户统计", description = "获取用户相关统计数据，包括新增用户、活跃用户等")
    @GetMapping("/user")
    public Result<UserStatsVO> getUserStats() {
        return Result.success(statsService.getUserStats());
    }

    /**
     * 获取帖子统计
     */
    @Operation(summary = "获取帖子统计", description = "获取帖子相关统计数据，包括发帖量、热门帖子等")
    @GetMapping("/post")
    public Result<PostStatsVO> getPostStats() {
        return Result.success(statsService.getPostStats());
    }

    /**
     * 获取互动统计
     */
    @Operation(summary = "获取互动统计", description = "获取互动相关统计数据，包括点赞、评论、收藏等")
    @GetMapping("/interaction")
    public Result<InteractionStatsVO> getInteractionStats() {
        return Result.success(statsService.getInteractionStats());
    }

    /**
     * 获取趋势数据
     */
    @Operation(summary = "获取趋势数据", description = "获取各类数据的趋势变化，支持按日/周/月查询")
    @GetMapping("/trend")
    public Result<TrendDataVO> getTrend(
            @Parameter(description = "时间范围类型：day-近7天，week-近4周，month-近12月")
            @RequestParam(value = "rangeType", defaultValue = "day") String rangeType) {
        return Result.success(statsService.getTrend(rangeType));
    }

    /**
     * 导出报表
     */
    @Operation(summary = "导出报表", description = "导出统计数据报表为Excel文件")
    @GetMapping("/export")
    public void exportReport(
            @Parameter(description = "统计类型：overview-概览，user-用户，post-帖子，interaction-互动")
            @RequestParam(value = "type", defaultValue = "overview") String statsType,
            HttpServletResponse response) {
        statsService.exportReport(statsType, response);
    }
}
