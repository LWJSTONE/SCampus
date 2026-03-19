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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * 创建分类
     */
    @PostMapping("/categories")
    @Operation(summary = "创建分类", description = "创建新的版块分类")
    public Result<Long> createCategory(
            @Parameter(description = "分类信息", required = true) @Validated @RequestBody CategoryDTO dto,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("创建分类：{}", dto.getName());
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("更新分类：{}", id);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("删除分类：{}", id);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("更新分类状态：{}，状态：{}", id, status);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("创建版块：{}", dto.getName());
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("更新版块：{}", id);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("删除版块：{}", id);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("更新版块状态：{}，状态：{}", id, status);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("添加版主：版块={}，用户={}", id, dto.getUserId());
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("移除版主：版块={}，用户={}", id, userId);
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
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("设置主版主：版块={}，用户={}", id, userId);
        boolean result = moderatorService.setPrimaryModerator(id, userId);
        return Result.success(result);
    }
}
