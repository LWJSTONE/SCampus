package com.campus.forum.controller;

import com.campus.forum.constant.Constants;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.service.StatsService;
import com.campus.forum.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     * 内部服务密钥，用于服务间调用的安全验证
     */
    @Value("${app.internal-service-key:campus-internal-secret-key-2024}")
    private String internalServiceKey;

    /**
     * 趋势查询时间范围类型白名单
     */
    private static final Set<String> VALID_RANGE_TYPES = new HashSet<>(Arrays.asList("day", "week", "month"));

    /**
     * 报表导出统计类型白名单
     */
    private static final Set<String> VALID_STATS_TYPES = new HashSet<>(Arrays.asList("overview", "user", "post", "interaction"));

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
        // 参数校验：rangeType白名单校验
        if (!VALID_RANGE_TYPES.contains(rangeType.toLowerCase())) {
            log.warn("无效的rangeType参数: {}, 允许的值: {}", rangeType, VALID_RANGE_TYPES);
            return Result.fail(400, "无效的时间范围类型，仅支持: day, week, month");
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
            throw new BusinessException(403, "无权限执行此操作，需要管理员权限");
        }
        // 参数校验：statsType白名单校验
        if (!VALID_STATS_TYPES.contains(statsType.toLowerCase())) {
            log.warn("无效的statsType参数: {}, 允许的值: {}", statsType, VALID_STATS_TYPES);
            throw new BusinessException(400, "无效的统计类型，仅支持: overview, user, post, interaction");
        }
        statsService.exportReport(statsType, response);
    }

    /**
     * 检查当前用户是否为管理员
     * 增强验证方式：同时检查内部服务密钥和用户角色
     * 
     * 验证逻辑：
     * 1. 内部服务密钥验证（服务间调用）：通过X-Internal-Service-Key请求头
     * 2. 用户角色验证（用户请求）：通过X-User-Role请求头或request.getAttribute
     * 
     * 两种方式任一通过即可，以支持不同调用场景
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 方式1：内部服务密钥验证（用于服务间内部调用）
        String internalKey = request.getHeader("X-Internal-Service-Key");
        if (internalKey != null && !internalKey.isEmpty()) {
            if (internalServiceKey.equals(internalKey)) {
                log.debug("内部服务密钥验证通过");
                return true;
            } else {
                log.warn("内部服务密钥验证失败，可能存在伪造请求");
            }
        }
        
        // 方式2：用户角色验证（用于用户请求，由网关传递）
        // 优先从Header获取（网关传递）
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader != null && !roleHeader.isEmpty()) {
            boolean isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(roleHeader) 
                    || Constants.ROLE_SUPER_ADMIN.equalsIgnoreCase(roleHeader)
                    || ("ROLE_" + Constants.ROLE_ADMIN).equalsIgnoreCase(roleHeader)
                    || ("ROLE_" + Constants.ROLE_SUPER_ADMIN).equalsIgnoreCase(roleHeader);
            if (isAdmin) {
                log.debug("用户角色验证通过: {}", roleHeader);
            }
            return isAdmin;
        }
        
        // 回退到request.getAttribute获取
        Object userRole = request.getAttribute("userRole");
        if (userRole != null) {
            String role = userRole.toString();
            boolean isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(role) 
                    || Constants.ROLE_SUPER_ADMIN.equalsIgnoreCase(role)
                    || ("ROLE_" + Constants.ROLE_ADMIN).equalsIgnoreCase(role)
                    || ("ROLE_" + Constants.ROLE_SUPER_ADMIN).equalsIgnoreCase(role);
            if (isAdmin) {
                log.debug("用户角色验证通过(attribute): {}", role);
            }
            return isAdmin;
        }
        
        log.debug("权限验证失败：未找到有效的身份验证信息");
        return false;
    }
}
