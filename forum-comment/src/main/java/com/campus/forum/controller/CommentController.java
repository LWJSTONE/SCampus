package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.CommentCreateDTO;
import com.campus.forum.dto.CommentQueryDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.service.CommentService;
import com.campus.forum.utils.IpUtils;
import com.campus.forum.vo.CommentVO;
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
 * 评论控制器
 * 
 * 提供评论相关的REST API接口
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取帖子评论列表
     * 
     * @param postId 帖子ID
     * @param current 当前页
     * @param size 每页大小
     * @param request HTTP请求
     * @return 评论列表
     */
    @GetMapping("/post/{postId}")
    @Operation(summary = "获取帖子评论列表", description = "分页获取指定帖子的评论列表，包含子评论")
    public Result<IPage<CommentVO>> getPostComments(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序方式") @RequestParam(required = false) String sortBy,
            HttpServletRequest request) {
        
        log.info("获取帖子评论列表, postId: {}, current: {}, size: {}", postId, current, size);
        
        // 构建查询参数
        CommentQueryDTO queryDTO = new CommentQueryDTO();
        queryDTO.setPostId(postId);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setSortBy(sortBy);
        
        // 获取当前用户ID（从请求属性中获取，由网关传递）
        Long currentUserId = getCurrentUserId(request);
        
        // 查询评论列表
        IPage<CommentVO> commentPage = commentService.getCommentsByPostId(postId, queryDTO, currentUserId);
        
        return Result.success(commentPage);
    }

    /**
     * 发布评论
     * 
     * @param createDTO 评论创建DTO
     * @param request HTTP请求
     * @return 评论ID
     */
    @PostMapping
    @Operation(summary = "发布评论", description = "发布新评论，支持一级评论和楼中楼回复")
    public Result<Long> publishComment(
            @Validated @RequestBody CommentCreateDTO createDTO,
            HttpServletRequest request) {
        
        log.info("发布评论, postId: {}, parentId: {}", createDTO.getPostId(), createDTO.getParentId());
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 获取IP地址
        String ipAddress = IpUtils.getIpAddr(request);
        
        // 发布评论
        Long commentId = commentService.publishComment(createDTO, userId, ipAddress);
        
        return Result.success("评论发布成功", commentId);
    }

    /**
     * 删除评论
     * 
     * @param id 评论ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除指定评论（只能删除自己的评论）")
    public Result<Boolean> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("删除评论, commentId: {}", id);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 删除评论
        boolean result = commentService.deleteComment(id, userId);
        
        return Result.success("删除成功", result);
    }

    /**
     * 点赞/取消点赞评论
     * 
     * @param id 评论ID
     * @param request HTTP请求
     * @return 操作结果（true-点赞成功，false-取消点赞）
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评论", description = "点赞或取消点赞评论")
    public Result<Map<String, Object>> likeComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("点赞评论, commentId: {}", id);
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        
        // 点赞/取消点赞
        boolean isLike = commentService.likeComment(id, userId);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("isLike", isLike);
        result.put("message", isLike ? "点赞成功" : "已取消点赞");
        
        return Result.success(result);
    }

    /**
     * 获取评论的回复列表
     * 
     * @param id 评论ID
     * @param current 当前页
     * @param size 每页大小
     * @param request HTTP请求
     * @return 回复列表
     */
    @GetMapping("/{id}/replies")
    @Operation(summary = "获取评论回复列表", description = "分页获取指定评论的回复列表")
    public Result<IPage<CommentVO>> getCommentReplies(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        log.info("获取评论回复列表, commentId: {}, current: {}, size: {}", id, current, size);
        
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);
        
        // 查询回复列表
        IPage<CommentVO> replyPage = commentService.getReplies(id, current, size, currentUserId);
        
        return Result.success(replyPage);
    }

    /**
     * 获取评论详情
     * 
     * @param id 评论ID
     * @param request HTTP请求
     * @return 评论详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评论详情", description = "获取指定评论的详细信息")
    public Result<CommentVO> getCommentDetail(
            @Parameter(description = "评论ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        log.info("获取评论详情, commentId: {}", id);
        
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);
        
        // 查询评论详情
        CommentVO comment = commentService.getCommentDetail(id, currentUserId);
        
        return Result.success(comment);
    }

    /**
     * 批量获取帖子评论数
     * 
     * @param postIds 帖子ID列表
     * @return 帖子ID与评论数的映射
     */
    @PostMapping("/count/batch")
    @Operation(summary = "批量获取评论数", description = "批量获取多个帖子的评论数量")
    public Result<Map<Long, Integer>> batchGetCommentCount(
            @RequestBody List<Long> postIds) {
        
        log.info("批量获取评论数, postIds: {}", postIds);
        
        Map<Long, Integer> countMap = commentService.countByPostIds(postIds);
        
        return Result.success(countMap);
    }

    /**
     * 检查是否已点赞
     * 
     * @param id 评论ID
     * @param request HTTP请求
     * @return 是否已点赞
     */
    @GetMapping("/{id}/liked")
    @Operation(summary = "检查是否已点赞", description = "检查当前用户是否已点赞指定评论")
    public Result<Map<String, Object>> checkLiked(
            @Parameter(description = "评论ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", false);
            return Result.success(result);
        }
        
        boolean isLiked = commentService.isLiked(id, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        
        return Result.success(result);
    }

    /**
     * 获取用户的评论列表
     * 
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户评论列表", description = "分页获取指定用户发表的评论列表")
    public Result<IPage<CommentVO>> getUserComments(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        log.info("获取用户评论列表, userId: {}, current: {}, size: {}", userId, current, size);
        
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);
        
        // 查询用户评论列表
        IPage<CommentVO> commentPage = commentService.getCommentsByUserId(userId, current, size, currentUserId);
        
        return Result.success(commentPage);
    }

    /**
     * 审核评论（管理员）
     * 
     * @param id 评论ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/audit")
    @Operation(summary = "审核评论", description = "管理员审核评论状态（通过/驳回）")
    public Result<Boolean> auditComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(description = "审核状态（1-通过，2-驳回）") @RequestParam Integer status,
            @Parameter(description = "审核备注") @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        
        log.info("审核评论, commentId: {}, status: {}", id, status);
        
        // 验证状态值
        if (status != 1 && status != 2) {
            return Result.fail(400, "状态值无效，只能为1（通过）或2（驳回）");
        }
        
        // 权限校验：只有管理员可以审核评论
        String userRole = request.getHeader("X-User-Role");
        if (userRole == null || (!"ADMIN".equals(userRole) && !"SUPER_ADMIN".equals(userRole))) {
            log.warn("非管理员尝试审核评论，userRole: {}", userRole);
            return Result.fail(403, "权限不足，只有管理员可以审核评论");
        }
        
        // 审核评论
        boolean result = commentService.auditComment(id, status, remark);
        
        return Result.success("审核完成", result);
    }

    // ==================== 私有方法 ====================

    /**
     * 从请求中获取当前用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID（未登录返回null）
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 从请求头获取用户ID（由网关解析JWT后传递）
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("解析用户ID失败: {}", userIdStr);
            }
        }
        
        // 从请求属性获取（可选方式）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        
        return null;
    }
}
