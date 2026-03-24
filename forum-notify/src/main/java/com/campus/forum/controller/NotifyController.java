package com.campus.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.NoticeCreateDTO;
import com.campus.forum.dto.NoticeQueryDTO;
import com.campus.forum.dto.NoticeUpdateDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.service.NoticeService;
import com.campus.forum.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 * 
 * 提供通知相关的REST API接口
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotifyController {

    private final NoticeService noticeService;

    /**
     * 获取通知列表
     * 
     * @param type 通知类型
     * @param current 当前页
     * @param size 每页大小
     * @param request HTTP请求
     * @return 通知列表
     */
    @GetMapping
    @Operation(summary = "获取通知列表", description = "分页获取通知列表")
    public Result<Page<NoticeVO>> getNoticeList(
            @Parameter(description = "通知类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        // 【修复】分页参数有效性校验，防止非法参数
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10; // 默认每页10条，最大100条
        }
        
        log.info("获取通知列表, type: {}, current: {}, size: {}", type, current, size);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        
        // 构建查询参数
        NoticeQueryDTO queryDTO = new NoticeQueryDTO();
        queryDTO.setType(type);
        queryDTO.setStatus(1); // 只查询已发布的
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        
        // 查询通知列表
        Page<NoticeVO> noticePage = noticeService.getNoticeList(queryDTO, userId);
        
        return Result.success(noticePage);
    }

    /**
     * 获取通知详情
     * 
     * @param id 通知ID
     * @param request HTTP请求
     * @return 通知详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情", description = "获取指定通知的详细信息")
    public Result<NoticeVO> getNoticeDetail(
            @Parameter(description = "通知ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("获取通知详情, id: {}", id);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        
        // 查询通知详情
        NoticeVO notice = noticeService.getNoticeDetail(id, userId);
        
        return Result.success(notice);
    }

    /**
     * 发布通知
     * 
     * @param createDTO 通知创建DTO
     * @param request HTTP请求
     * @return 通知ID
     */
    @PostMapping
    @Operation(summary = "发布通知", description = "发布新通知（管理员功能）")
    public Result<Long> publishNotice(
            @Validated @RequestBody NoticeCreateDTO createDTO,
            HttpServletRequest request) {
        
        log.info("发布通知, title: {}", createDTO.getTitle());
        
        // 获取当前用户信息
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);
        
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 权限校验：只有管理员可以发布通知
        if (!isAdmin(request)) {
            log.warn("非管理员尝试发布通知: userId={}", userId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 发布通知
        Long noticeId = noticeService.publishNotice(createDTO, userId, userName);
        
        return Result.success("发布成功", noticeId);
    }

    /**
     * 更新通知
     * 
     * @param id 通知ID
     * @param updateDTO 通知更新DTO
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新通知", description = "更新通知信息（管理员功能）")
    public Result<Boolean> updateNotice(
            @Parameter(description = "通知ID") @PathVariable Long id,
            @Validated @RequestBody NoticeUpdateDTO updateDTO,
            HttpServletRequest request) {
        
        log.info("更新通知, id: {}", id);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 权限校验：只有管理员可以更新通知
        if (!isAdmin(request)) {
            log.warn("非管理员尝试更新通知: userId={}, noticeId={}", userId, id);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 更新通知
        boolean result = noticeService.updateNotice(id, updateDTO, userId);
        
        return Result.success("更新成功", result);
    }

    /**
     * 删除通知
     * 
     * @param id 通知ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除通知（管理员功能）")
    public Result<Boolean> deleteNotice(
            @Parameter(description = "通知ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("删除通知, id: {}", id);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 权限校验：只有管理员可以删除通知
        if (!isAdmin(request)) {
            log.warn("非管理员尝试删除通知: userId={}, noticeId={}", userId, id);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 删除通知
        boolean result = noticeService.deleteNotice(id, userId);
        
        return Result.success("删除成功", result);
    }

    /**
     * 获取未读消息数
     * 
     * @param request HTTP请求
     * @return 未读消息数
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息数量")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("count", 0);
            return Result.success(result);
        }
        
        // 获取未读消息数
        int count = noticeService.getUnreadCount(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        
        return Result.success(result);
    }

    /**
     * 标记通知已读
     * 
     * @param id 通知ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记指定通知为已读")
    public Result<Boolean> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("标记通知已读, id: {}", id);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 标记已读
        boolean result = noticeService.markAsRead(id, userId);
        
        return Result.success("标记成功", result);
    }

    /**
     * 全部标记已读
     * 
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/read/all")
    @Operation(summary = "全部标记已读", description = "将所有通知标记为已读")
    public Result<Boolean> markAllAsRead(HttpServletRequest request) {
        
        log.info("全部标记已读");
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 全部标记已读
        boolean result = noticeService.markAllAsRead(userId);
        
        return Result.success("操作成功", result);
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
     * 从请求中获取当前用户名称
     */
    private String getCurrentUserName(HttpServletRequest request) {
        String userName = request.getHeader("X-User-Name");
        if (userName != null && !userName.isEmpty()) {
            return userName;
        }
        
        Object userNameAttr = request.getAttribute("userName");
        if (userNameAttr instanceof String) {
            return (String) userNameAttr;
        }
        
        return "系统管理员";
    }

    /**
     * 检查当前用户是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role != null) {
            return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        }
        return false;
    }
}
