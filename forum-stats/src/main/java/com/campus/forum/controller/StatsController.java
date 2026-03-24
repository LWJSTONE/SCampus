package com.campus.forum.controller;

import com.campus.forum.entity.Result;
import com.campus.forum.service.StatsService;
import com.campus.forum.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
 * 【修复】所有统计接口添加管理员权限校验
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "统计接口", description = "系统统计数据相关接口（需要管理员权限）")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取系统概览统计
     */
    @Operation(summary = "获取系统概览统计", description = "获取系统整体数据概览，包括用户数、帖子数、评论数等（需要管理员权限）")
    @GetMapping("/overview")
    public Result<OverviewStatsVO> getOverview(HttpServletRequest request) {
        // 权限验证：只有管理员可以查看统计
        if (!isAdmin(request)) {
            log.warn("非管理员尝试访问统计接口: overview");
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        return Result.success(statsService.getOverview());
    }

    /**
     * 获取用户统计
     */
    @Operation(summary = "获取用户统计", description = "获取用户相关统计数据，包括新增用户、活跃用户等（需要管理员权限）")
    @GetMapping("/user")
    public Result<UserStatsVO> getUserStats(HttpServletRequest request) {
        // 权限验证：只有管理员可以查看统计
        if (!isAdmin(request)) {
            log.warn("非管理员尝试访问统计接口: user");
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        return Result.success(statsService.getUserStats());
    }

    /**
     * 获取帖子统计
     */
    @Operation(summary = "获取帖子统计", description = "获取帖子相关统计数据，包括发帖量、热门帖子等（需要管理员权限）")
    @GetMapping("/post")
    public Result<PostStatsVO> getPostStats(HttpServletRequest request) {
        // 权限验证：只有管理员可以查看统计
        if (!isAdmin(request)) {
            log.warn("非管理员尝试访问统计接口: post");
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        return Result.success(statsService.getPostStats());
    }

    /**
     * 获取互动统计
     */
    @Operation(summary = "获取互动统计", description = "获取互动相关统计数据，包括点赞、评论、收藏等（需要管理员权限）")
    @GetMapping("/interaction")
    public Result<InteractionStatsVO> getInteractionStats(HttpServletRequest request) {
        // 权限验证：只有管理员可以查看统计
        if (!isAdmin(request)) {
            log.warn("非管理员尝试访问统计接口: interaction");
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        return Result.success(statsService.getInteractionStats());
    }

    /**
     * 获取趋势数据
     */
    @Operation(summary = "获取趋势数据", description = "获取各类数据的趋势变化，支持按日/周/月查询（需要管理员权限）")
    @GetMapping("/trend")
    public Result<TrendDataVO> getTrend(
            @Parameter(description = "时间范围类型：day-近7天，week-近4周，month-近12月")
            @RequestParam(value = "rangeType", defaultValue = "day") String rangeType,
            HttpServletRequest request) {
        // 权限验证：只有管理员可以查看统计
        if (!isAdmin(request)) {
            log.warn("非管理员尝试访问统计接口: trend");
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        return Result.success(statsService.getTrend(rangeType));
    }

    /**
     * 导出报表
     */
    @Operation(summary = "导出报表", description = "导出统计数据报表为Excel文件（需要管理员权限）")
    @GetMapping("/export")
    public void exportReport(
            @Parameter(description = "统计类型：overview-概览，user-用户，post-帖子，interaction-互动")
            @RequestParam(value = "type", defaultValue = "overview") String statsType,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 权限验证：只有管理员可以导出报表
        if (!isAdmin(request)) {
            log.warn("非管理员尝试访问统计接口: export");
            throw new RuntimeException("无权限执行此操作，需要管理员权限");
        }
        statsService.exportReport(statsType, response);
    }

    /**
     * 检查当前用户是否为管理员
     * 同时检查Header（网关传递）和request.getAttribute
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 优先从Header获取（网关传递）
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader != null && !roleHeader.isEmpty()) {
            return "ADMIN".equalsIgnoreCase(roleHeader) || "ROLE_ADMIN".equalsIgnoreCase(roleHeader);
        }
        
        // 回退到request.getAttribute获取
        Object userRole = request.getAttribute("userRole");
        if (userRole != null) {
            String role = userRole.toString();
            return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        }
        
        return false;
    }
}
