package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.*;
import com.campus.forum.entity.Result;
import com.campus.forum.service.ApproveService;
import com.campus.forum.service.ReportService;
import com.campus.forum.service.UserBanService;
import com.campus.forum.vo.ApproveVO;
import com.campus.forum.vo.ReportVO;
import com.campus.forum.vo.UserBanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 举报审核控制器
 *
 * 提供举报、审核、禁言相关的REST API接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "举报审核管理", description = "举报审核相关接口")
public class ReportController {

    private final ReportService reportService;
    private final ApproveService approveService;
    private final UserBanService userBanService;

    // ==================== 举报相关接口 ====================

    /**
     * 提交举报
     */
    @PostMapping
    @Operation(summary = "提交举报", description = "用户提交内容举报")
    public Result<Long> submitReport(
            @Validated @RequestBody ReportCreateDTO createDTO,
            HttpServletRequest request) {
        
        log.info("提交举报, targetId: {}, type: {}", createDTO.getTargetId(), createDTO.getReportType());
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        Long reportId = reportService.submitReport(createDTO, userId);
        
        return Result.success("举报提交成功，我们会尽快处理", reportId);
    }

    /**
     * 获取举报列表
     */
    @GetMapping
    @Operation(summary = "获取举报列表", description = "分页获取举报列表（管理员）")
    public Result<IPage<ReportVO>> getReportList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "处理状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "举报类型") @RequestParam(required = false) Integer reportType,
            @Parameter(description = "原因类型") @RequestParam(required = false) Integer reasonType,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("获取举报列表, current: {}, size: {}", current, size);
        
        ReportQueryDTO queryDTO = new ReportQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setReportType(reportType);
        queryDTO.setReasonType(reasonType);
        
        IPage<ReportVO> page = reportService.getReportPage(queryDTO);
        
        return Result.success(page);
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取举报详情", description = "获取指定举报的详细信息（管理员）")
    public Result<ReportVO> getReportDetail(
            @Parameter(description = "举报ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        // 验证管理员权限（举报详情包含敏感信息，仅管理员可查看）
        validateAdminPermission(request);
        
        log.info("获取举报详情, id: {}", id);
        
        ReportVO reportVO = reportService.getReportDetail(id);
        
        return Result.success(reportVO);
    }

    /**
     * 处理举报
     */
    @PutMapping("/{id}/handle")
    @Operation(summary = "处理举报", description = "管理员处理举报")
    public Result<Boolean> handleReport(
            @Parameter(description = "举报ID") @PathVariable Long id,
            @Validated @RequestBody ReportHandleDTO handleDTO,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("处理举报, id: {}, result: {}", id, handleDTO.getResult());
        
        Long handlerId = getCurrentUserId(request);
        if (handlerId == null) {
            return Result.fail(401, "请先登录");
        }
        
        boolean result = reportService.handleReport(id, handleDTO, handlerId);
        
        return Result.success("处理成功", result);
    }

    /**
     * 获取待处理统计
     */
    @GetMapping("/pending-count")
    @Operation(summary = "获取待处理统计", description = "获取待处理的举报和审核数量")
    public Result<Map<String, Object>> getPendingCount(HttpServletRequest request) {
        
        int reportCount = reportService.getPendingCount();
        int approveCount = approveService.getPendingCount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("pendingReportCount", reportCount);
        result.put("pendingApproveCount", approveCount);
        result.put("totalPendingCount", reportCount + approveCount);
        
        return Result.success(result);
    }

    // ==================== 审核相关接口 ====================

    /**
     * 获取待审核列表
     */
    @GetMapping("/approve")
    @Operation(summary = "获取待审核列表", description = "分页获取待审核内容列表")
    public Result<IPage<ApproveVO>> getApproveList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "内容类型") @RequestParam(required = false) Integer contentType,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("获取待审核列表, current: {}, size: {}", current, size);
        
        ApproveQueryDTO queryDTO = new ApproveQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setContentType(contentType);
        queryDTO.setUserId(userId);
        
        IPage<ApproveVO> page = approveService.getApprovePage(queryDTO);
        
        return Result.success(page);
    }

    /**
     * 审核通过/驳回
     */
    @PutMapping("/approve/{id}")
    @Operation(summary = "审核通过/驳回", description = "管理员审核内容")
    public Result<Boolean> approve(
            @Parameter(description = "审核ID") @PathVariable Long id,
            @Validated @RequestBody ApproveHandleDTO handleDTO,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("审核处理, id: {}, status: {}", id, handleDTO.getStatus());
        
        Long auditorId = getCurrentUserId(request);
        if (auditorId == null) {
            return Result.fail(401, "请先登录");
        }
        
        boolean result = approveService.approve(id, handleDTO, auditorId);
        
        return Result.success("审核完成", result);
    }

    /**
     * 获取审核详情
     */
    @GetMapping("/approve/{id}")
    @Operation(summary = "获取审核详情", description = "获取指定审核记录的详细信息")
    public Result<ApproveVO> getApproveDetail(
            @Parameter(description = "审核ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("获取审核详情, id: {}", id);
        
        ApproveVO approveVO = approveService.getApproveDetail(id);
        
        return Result.success(approveVO);
    }

    // ==================== 禁言相关接口 ====================

    /**
     * 禁言用户
     */
    @PostMapping("/ban")
    @Operation(summary = "禁言用户", description = "管理员禁言用户")
    public Result<Long> banUser(
            @Validated @RequestBody UserBanDTO banDTO,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("禁言用户, userId: {}, days: {}", banDTO.getUserId(), banDTO.getBanDays());
        
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }
        
        Long banId = userBanService.banUser(banDTO, operatorId);
        
        return Result.success("禁言成功", banId);
    }

    /**
     * 解除禁言
     */
    @DeleteMapping("/ban/{userId}")
    @Operation(summary = "解除禁言", description = "管理员解除用户禁言")
    public Result<Boolean> unbanUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "解除原因") @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("解除禁言, userId: {}", userId);
        
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }
        
        boolean result = userBanService.unbanUser(userId, operatorId, reason);
        
        return Result.success("解除禁言成功", result);
    }

    /**
     * 获取用户禁言状态
     */
    @GetMapping("/ban/user/{userId}")
    @Operation(summary = "获取用户禁言状态", description = "查询用户当前禁言状态")
    public Result<UserBanVO> getUserBanStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            HttpServletRequest request) {
        
        log.info("获取用户禁言状态, userId: {}", userId);
        
        UserBanVO banVO = userBanService.getActiveBan(userId);
        
        return Result.success(banVO);
    }

    /**
     * 获取禁言列表
     */
    @GetMapping("/ban")
    @Operation(summary = "获取禁言列表", description = "分页获取禁言记录列表")
    public Result<IPage<UserBanVO>> getBanList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "禁言状态") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        // 验证管理员权限
        validateAdminPermission(request);
        
        log.info("获取禁言列表, current: {}, size: {}", current, size);
        
        IPage<UserBanVO> page = userBanService.getBanPage(userId, status, current, size);
        
        return Result.success(page);
    }

    /**
     * 获取用户禁言历史
     */
    @GetMapping("/ban/history/{userId}")
    @Operation(summary = "获取用户禁言历史", description = "查询用户禁言历史记录")
    public Result<List<UserBanVO>> getBanHistory(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            HttpServletRequest request) {
        
        log.info("获取用户禁言历史, userId: {}", userId);
        
        List<UserBanVO> history = userBanService.getBanHistory(userId);
        
        return Result.success(history);
    }

    // ==================== 私有方法 ====================

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("解析用户ID失败: {}", userIdStr);
            }
        }
        
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        
        return null;
    }

    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null || (!"ADMIN".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role))) {
            throw new RuntimeException("无权限执行此操作，需要管理员权限");
        }
    }
}
