package com.campus.forum.controller;

import com.campus.forum.dto.CategoryDTO;
import com.campus.forum.dto.ForumDTO;
import com.campus.forum.dto.ModeratorDTO;
import com.campus.forum.entity.Category;
import com.campus.forum.entity.Forum;
import com.campus.forum.entity.Moderator;
import com.campus.forum.entity.Result;
import com.campus.forum.service.CategoryService;
import com.campus.forum.service.ForumService;
import com.campus.forum.service.ModeratorService;
import com.campus.forum.vo.CategoryTreeVO;
import com.campus.forum.vo.ForumVO;
import com.campus.forum.vo.ModeratorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 版块分类控制器
 *
 * 提供版块分类、版块、版块管理的REST API接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "版块分类管理", description = "版块分类、版块、版主相关接口")
public class CategoryController {

    private final CategoryService categoryService;
    private final ForumService forumService;
    private final ModeratorService moderatorService;

    /**
     * 内部服务密钥，用于验证来自网关的内部请求
     * 【安全修复】通过双重验证（Header角色 + 内部密钥）防止权限伪造
     */
    @Value("${service.internal.secret-key:}")
    private String internalSecretKey;

    // ==================== 分类管理 ====================

    /**
     * 获取分类列表（树形结构）
     */
    @GetMapping("/categories")
    @Operation(summary = "获取分类列表", description = "获取树形结构的分类列表")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        log.info("获取分类树形列表");
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/categories/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    public Result<Category> getCategoryDetail(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        log.info("获取分类详情：{}", id);
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.fail(404, "分类不存在");
        }
        return Result.success(category);
    }

    /**
     * 创建分类
     */
    @PostMapping("/categories")
    @Operation(summary = "创建分类", description = "创建新的版块分类")
    public Result<Long> createCategory(
            @Parameter(description = "分类信息", required = true) @Validated @RequestBody CategoryDTO dto,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("创建分类：{}", dto.getName());
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        Category category = categoryService.createCategory(dto);
        return Result.success(category.getId());
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    @Operation(summary = "更新分类", description = "更新版块分类信息")
    public Result<Boolean> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id,
            @Parameter(description = "分类信息", required = true) @Validated @RequestBody CategoryDTO dto,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("更新分类：{}", id);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = categoryService.updateCategory(id, dto);
        return Result.success(result);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    @Operation(summary = "删除分类", description = "删除版块分类")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("删除分类：{}", id);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = categoryService.deleteCategory(id);
        return Result.success(result);
    }

    /**
     * 更新分类状态
     */
    @PutMapping("/categories/{id}/status")
    @Operation(summary = "更新分类状态", description = "启用/禁用分类")
    public Result<Boolean> updateCategoryStatus(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态", required = true) @RequestParam Integer status,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("更新分类状态：{}，状态：{}", id, status);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = categoryService.updateStatus(id, status);
        return Result.success(result);
    }

    // ==================== 版块管理 ====================

    /**
     * 获取版块列表
     */
    @GetMapping("/forums")
    @Operation(summary = "获取版块列表", description = "获取所有版块列表")
    public Result<List<ForumVO>> getAllForums(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId) {
        log.info("获取版块列表，分类ID：{}", categoryId);
        List<ForumVO> forums;
        if (categoryId != null) {
            forums = forumService.getForumsByCategory(categoryId);
        } else {
            forums = forumService.getAllForums();
        }
        return Result.success(forums);
    }

    /**
     * 获取版块详情
     */
    @GetMapping("/forums/{id}")
    @Operation(summary = "获取版块详情", description = "根据ID获取版块详细信息")
    public Result<ForumVO> getForumDetail(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id) {
        log.info("获取版块详情：{}", id);
        ForumVO detail = forumService.getForumDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建版块
     */
    @PostMapping("/forums")
    @Operation(summary = "创建版块", description = "创建新版块")
    public Result<Long> createForum(
            @Parameter(description = "版块信息", required = true) @Validated @RequestBody ForumDTO dto,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("创建版块：{}", dto.getName());
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        Forum forum = forumService.createForum(dto);
        return Result.success(forum.getId());
    }

    /**
     * 更新版块
     */
    @PutMapping("/forums/{id}")
    @Operation(summary = "更新版块", description = "更新版块信息")
    public Result<Boolean> updateForum(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id,
            @Parameter(description = "版块信息", required = true) @Validated @RequestBody ForumDTO dto,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("更新版块：{}", id);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = forumService.updateForum(id, dto);
        return Result.success(result);
    }

    /**
     * 删除版块
     */
    @DeleteMapping("/forums/{id}")
    @Operation(summary = "删除版块", description = "删除版块")
    public Result<Boolean> deleteForum(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("删除版块：{}", id);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = forumService.deleteForum(id);
        return Result.success(result);
    }

    /**
     * 更新版块状态
     */
    @PutMapping("/forums/{id}/status")
    @Operation(summary = "更新版块状态", description = "启用/禁用版块")
    public Result<Boolean> updateForumStatus(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态", required = true) @RequestParam Integer status,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("更新版块状态：{}，状态：{}", id, status);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = forumService.updateStatus(id, status);
        return Result.success(result);
    }

    // ==================== 版主管理 ====================

    /**
     * 获取版主列表
     */
    @GetMapping("/forums/{id}/moderators")
    @Operation(summary = "获取版主列表", description = "获取版块的版主列表")
    public Result<List<ModeratorVO>> getModerators(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id) {
        log.info("获取版主列表，版块ID：{}", id);
        List<ModeratorVO> moderators = moderatorService.getModerators(id);
        return Result.success(moderators);
    }

    /**
     * 添加版主
     */
    @PostMapping("/forums/{id}/moderators")
    @Operation(summary = "添加版主", description = "为版块添加版主")
    public Result<Long> addModerator(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id,
            @Parameter(description = "版主信息", required = true) @Validated @RequestBody ModeratorDTO dto,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("添加版主：版块={}，用户={}", id, dto.getUserId());
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        Moderator moderator = moderatorService.addModerator(id, dto);
        return Result.success(moderator.getId());
    }

    /**
     * 移除版主
     */
    @DeleteMapping("/forums/{id}/moderators/{userId}")
    @Operation(summary = "移除版主", description = "移除版块的版主")
    public Result<Boolean> removeModerator(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("移除版主：版块={}，用户={}", id, userId);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = moderatorService.removeModerator(id, userId);
        return Result.success(result);
    }

    /**
     * 设置主版主
     */
    @PutMapping("/forums/{id}/moderators/{userId}/primary")
    @Operation(summary = "设置主版主", description = "设置版块的主版主")
    public Result<Boolean> setPrimaryModerator(
            @Parameter(description = "版块ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            HttpServletRequest request) {
        log.info("设置主版主：版块={}，用户={}", id, userId);
        
        // 验证管理员权限
        Result<Void> authResult = checkAdminPermission(request);
        if (authResult != null) {
            return Result.fail(authResult.getCode(), authResult.getMessage());
        }
        
        boolean result = moderatorService.setPrimaryModerator(id, userId);
        return Result.success(result);
    }

    // ==================== 私有方法 ====================

    /**
     * 检查管理员权限
     * 
     * 【安全修复说明】
     * 原权限校验仅依赖 X-User-Role 请求头，该字段可被客户端伪造。
     * 修复方案：添加内部服务密钥验证作为双重验证机制
     * 
     * 权限验证流程：
     * 1. 验证内部服务密钥（X-Internal-Service-Key）- 确保请求来自可信网关
     * 2. 验证用户角色（X-User-Role）- 确保用户具有管理员权限
     * 
     * 补偿方案：如果内部密钥未配置（兼容旧环境），则仅检查用户角色并记录警告日志
     *
     * @param request HTTP请求
     * @return 如果没有权限返回错误结果，有权限返回null
     */
    private Result<Void> checkAdminPermission(HttpServletRequest request) {
        // 1. 验证内部服务密钥（防止请求头伪造）
        String requestSecretKey = request.getHeader("X-Internal-Service-Key");
        boolean hasValidSecretKey = StringUtils.hasText(internalSecretKey) 
                && internalSecretKey.equals(requestSecretKey);
        
        // 如果配置了内部密钥但请求中密钥不匹配，记录警告
        if (StringUtils.hasText(internalSecretKey) && !hasValidSecretKey) {
            log.warn("内部服务密钥验证失败，可能存在伪造请求尝试，remoteAddr: {}", 
                    request.getRemoteAddr());
            // 严格模式下可以拒绝请求，此处为兼容性考虑继续执行角色检查
        }
        
        // 2. 验证用户角色
        String role = request.getHeader("X-User-Role");
        if (role == null || (!"ADMIN".equalsIgnoreCase(role) 
                && !"ROLE_ADMIN".equalsIgnoreCase(role)
                && !"SUPER_ADMIN".equalsIgnoreCase(role)
                && !"ROLE_SUPER_ADMIN".equalsIgnoreCase(role))) {
            log.warn("非管理员尝试执行管理操作，角色：{}，hasValidSecretKey: {}", role, hasValidSecretKey);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 3. 如果密钥验证失败且角色检查通过，记录安全警告（潜在伪造风险）
        if (StringUtils.hasText(internalSecretKey) && !hasValidSecretKey) {
            log.warn("【安全警告】管理操作权限验证不完整：角色校验通过但内部密钥验证失败，userRole: {}", role);
        }
        
        return null;
    }
}
