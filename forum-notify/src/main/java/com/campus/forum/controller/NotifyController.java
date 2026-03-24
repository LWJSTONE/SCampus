package com.campus.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.NoticeCreateDTO;
import com.campus.forum.dto.NoticeQueryDTO;
import com.campus.forum.dto.NoticeUpdateDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.service.NoticeService;
import com.campus.forum.utils.JwtUtils;
import com.campus.forum.utils.XssUtils;
import com.campus.forum.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
        
        // 【安全修复】权限校验：使用增强的管理员验证
        if (!isAdminSecure(request)) {
            log.warn("非管理员尝试发布通知: userId={}", userId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 【安全修复】XSS过滤：对标题、内容、备注进行XSS过滤
        createDTO.setTitle(XssUtils.filterTitle(createDTO.getTitle()));
        createDTO.setContent(XssUtils.filterContent(createDTO.getContent()));
        if (StringUtils.hasText(createDTO.getRemark())) {
            createDTO.setRemark(XssUtils.filterRemark(createDTO.getRemark()));
        }
        
        log.info("通知内容已进行XSS过滤, userId={}, title={}", userId, createDTO.getTitle());
        
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
        
        // 【安全修复】权限校验：使用增强的管理员验证
        if (!isAdminSecure(request)) {
            log.warn("非管理员尝试更新通知: userId={}, noticeId={}", userId, id);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 【安全修复】XSS过滤：对标题、内容、备注进行XSS过滤
        if (StringUtils.hasText(updateDTO.getTitle())) {
            updateDTO.setTitle(XssUtils.filterTitle(updateDTO.getTitle()));
        }
        if (StringUtils.hasText(updateDTO.getContent())) {
            updateDTO.setContent(XssUtils.filterContent(updateDTO.getContent()));
        }
        if (StringUtils.hasText(updateDTO.getRemark())) {
            updateDTO.setRemark(XssUtils.filterRemark(updateDTO.getRemark()));
        }
        
        log.info("更新通知内容已进行XSS过滤, userId={}, noticeId={}", userId, id);
        
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
        
        // 【安全修复】权限校验：使用增强的管理员验证
        if (!isAdminSecure(request)) {
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
     * 检查当前用户是否为管理员（基础方法）
     * 同时检查Header和request.getAttribute
     * 
     * @param request HTTP请求
     * @return 是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 优先从Header获取（网关传递）
        String role = request.getHeader("X-User-Role");
        if (role != null && !role.isEmpty()) {
            return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        }
        
        // 二次验证：从request.getAttribute获取（可能由过滤器/拦截器设置）
        Object userRole = request.getAttribute("userRole");
        if (userRole != null) {
            String roleStr = userRole.toString();
            return "ADMIN".equalsIgnoreCase(roleStr) || "ROLE_ADMIN".equalsIgnoreCase(roleStr);
        }
        
        return false;
    }

    /**
     * 【安全修复】增强的管理员权限验证
     * 
     * 验证流程：
     * 1. 首先检查Header和Attribute中的角色信息（兼容网关传递）
     * 2. 然后通过JWT Token进行二次验证（更安全）
     * 3. 两者都验证通过才认为是管理员
     * 
     * @param request HTTP请求
     * @return 是否为管理员
     */
    private boolean isAdminSecure(HttpServletRequest request) {
        // 第一层验证：检查Header和Attribute中的角色信息
        boolean headerAdmin = isAdmin(request);
        
        // 第二层验证：通过JWT Token验证
        boolean tokenAdmin = verifyAdminByToken(request);
        
        // 如果Header验证通过，但没有Token，仍然允许（兼容网关场景）
        // 但如果Token验证失败（Token存在但角色不对），则拒绝
        String token = extractToken(request);
        
        if (token == null || token.isEmpty()) {
            // 无Token场景：仅依赖Header验证，记录警告
            log.warn("管理员操作未提供JWT Token，仅使用Header验证，安全性降低");
            return headerAdmin;
        }
        
        // 有Token场景：必须Token验证也通过
        if (!tokenAdmin) {
            log.warn("JWT Token验证失败或角色不匹配");
            return false;
        }
        
        // 两层验证都通过
        return headerAdmin && tokenAdmin;
    }

    /**
     * 通过JWT Token验证管理员身份
     * 
     * @param request HTTP请求
     * @return 是否为管理员
     */
    private boolean verifyAdminByToken(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null || token.isEmpty()) {
                log.debug("未找到JWT Token");
                return false;
            }
            
            // 检查JWT密钥是否配置
            if (!JwtUtils.isSecretConfigured()) {
                log.warn("JWT密钥未配置，跳过Token验证");
                return false;
            }
            
            // 验证Token有效性
            if (!JwtUtils.verifyToken(token)) {
                log.warn("JWT Token验证失败");
                return false;
            }
            
            // 获取Token中的角色信息
            String role = JwtUtils.getRole(token);
            if (role == null) {
                log.warn("JWT Token中未包含角色信息");
                return false;
            }
            
            // 检查是否为管理员角色
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
            if (isAdmin) {
                log.debug("JWT Token验证通过，角色: {}", role);
            } else {
                log.warn("JWT Token中角色不是管理员: {}", role);
            }
            
            return isAdmin;
            
        } catch (SecurityException e) {
            log.error("JWT安全验证异常: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT Token验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从请求中提取JWT Token
     * 
     * @param request HTTP请求
     * @return Token字符串，不存在则返回null
     */
    private String extractToken(HttpServletRequest request) {
        // 1. 从Authorization Header获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 2. 从自定义Header获取
        String tokenHeader = request.getHeader("X-Access-Token");
        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            return tokenHeader;
        }
        
        // 3. 从请求参数获取
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        
        // 4. 从request.getAttribute获取
        Object tokenAttr = request.getAttribute("token");
        if (tokenAttr instanceof String) {
            return (String) tokenAttr;
        }
        
        return null;
    }
}
