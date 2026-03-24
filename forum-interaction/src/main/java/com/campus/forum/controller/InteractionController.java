package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.CollectDTO;
import com.campus.forum.dto.LikeDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.service.CollectService;
import com.campus.forum.service.LikeService;
import com.campus.forum.service.MentionService;
import com.campus.forum.vo.CollectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 互动控制器
 * 
 * 提供点赞、收藏、@提及相关的REST API接口
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
@Tag(name = "互动管理", description = "点赞、收藏、@提及相关接口")
public class InteractionController {

    private final LikeService likeService;
    private final CollectService collectService;
    private final MentionService mentionService;

    // ==================== 点赞相关接口 ====================

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    @Operation(summary = "点赞/取消点赞", description = "对帖子或评论进行点赞或取消点赞操作")
    public Result<Map<String, Object>> like(
            @Validated @RequestBody LikeDTO likeDTO,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        log.info("点赞操作: targetType={}, targetId={}, userId={}", 
                likeDTO.getTargetType(), likeDTO.getTargetId(), userId);
        
        boolean isLike = likeService.like(likeDTO.getTargetType(), likeDTO.getTargetId(), userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLike", isLike);
        result.put("message", isLike ? "点赞成功" : "已取消点赞");
        result.put("likeCount", likeService.getLikeCount(likeDTO.getTargetType(), likeDTO.getTargetId()));
        
        return Result.success(result);
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/like/check")
    @Operation(summary = "检查是否已点赞", description = "检查当前用户是否已点赞指定目标")
    public Result<Map<String, Object>> checkLike(
            @Parameter(description = "目标类型（1-帖子 2-评论）") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", userId != null && likeService.isLiked(targetType, targetId, userId));
        result.put("likeCount", likeService.getLikeCount(targetType, targetId));
        
        return Result.success(result);
    }

    /**
     * 获取点赞数量
     */
    @GetMapping("/like/count")
    @Operation(summary = "获取点赞数量", description = "获取指定目标的点赞数量")
    public Result<Map<String, Object>> getLikeCount(
            @Parameter(description = "目标类型（1-帖子 2-评论）") @RequestParam Integer targetType,
            @Parameter(description = "目标ID") @RequestParam Long targetId) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", likeService.getLikeCount(targetType, targetId));
        
        return Result.success(result);
    }

    // ==================== 收藏相关接口 ====================

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/collect")
    @Operation(summary = "收藏/取消收藏", description = "对帖子进行收藏或取消收藏操作")
    public Result<Map<String, Object>> collect(
            @Validated @RequestBody CollectDTO collectDTO,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        log.info("收藏操作: postId={}, userId={}", collectDTO.getPostId(), userId);
        
        boolean isCollect = collectService.collect(collectDTO.getPostId(), userId, collectDTO.getFolderId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("isCollect", isCollect);
        result.put("message", isCollect ? "收藏成功" : "已取消收藏");
        result.put("collectCount", collectService.getCollectCount(collectDTO.getPostId()));
        
        return Result.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/collect/check")
    @Operation(summary = "检查是否已收藏", description = "检查当前用户是否已收藏指定帖子")
    public Result<Map<String, Object>> checkCollect(
            @Parameter(description = "帖子ID") @RequestParam Long postId,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isCollected", userId != null && collectService.isCollected(postId, userId));
        result.put("collectCount", collectService.getCollectCount(postId));
        
        return Result.success(result);
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/collect/list")
    @Operation(summary = "获取收藏列表", description = "分页获取当前用户的收藏列表")
    public Result<IPage<CollectVO>> getCollectList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        log.info("获取收藏列表: userId={}, current={}, size={}", userId, current, size);
        
        IPage<CollectVO> collectPage = collectService.getCollectList(userId, current, size);
        
        return Result.success(collectPage);
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/collect/count")
    @Operation(summary = "获取收藏数量", description = "获取指定帖子的收藏数量")
    public Result<Map<String, Object>> getCollectCount(
            @Parameter(description = "帖子ID") @RequestParam Long postId) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("collectCount", collectService.getCollectCount(postId));
        
        return Result.success(result);
    }

    // ==================== @提及相关接口 ====================

    /**
     * 获取未读提及数
     */
    @GetMapping("/mention/unread")
    @Operation(summary = "获取未读提及数", description = "获取当前用户未读的@提及数量")
    public Result<Map<String, Object>> getUnreadMentionCount(HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        int unreadCount = mentionService.getUnreadCount(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", unreadCount);
        
        return Result.success(result);
    }

    /**
     * 标记所有提及为已读
     */
    @PostMapping("/mention/read/all")
    @Operation(summary = "标记所有提及为已读", description = "将当前用户的所有@提及标记为已读")
    public Result<Boolean> markAllMentionAsRead(HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        boolean success = mentionService.markAllAsRead(userId);
        
        return Result.success("操作成功", success);
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
}
