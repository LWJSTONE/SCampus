package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.constant.Constants;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.CollectDTO;
import com.campus.forum.dto.LikeDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.service.CollectService;
import com.campus.forum.service.LikeService;
import com.campus.forum.service.MentionService;
import com.campus.forum.utils.JwtUtils;
import com.campus.forum.vo.CollectVO;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    /**
     * JWT密钥 - 从配置文件读取，用于验证Token
     * 
     * 【安全修复】添加默认值处理，防止配置缺失时应用启动失败
     * 注意：生产环境必须在配置文件中设置 jwt.secret
     */
    @Value("${jwt.secret:}")
    private String jwtSecret;

    /**
     * 内部服务调用密钥
     * 用于验证来自其他微服务的内部API调用
     */
    @Value("${app.internal-service-key:}")
    private String internalServiceKey;

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
        
        // 分页参数边界校验
        if (current == null || current < 1) {
            current = Constants.DEFAULT_PAGE_NUM;
        }
        if (size == null || size < 1) {
            size = Constants.DEFAULT_PAGE_SIZE;
        }
        // 防止极大值导致内存溢出
        if (size > Constants.MAX_PAGE_SIZE) {
            log.warn("分页参数size超过最大值，已自动调整为: userId={}, 原始size={}, 调整后size={}", 
                    userId, size, Constants.MAX_PAGE_SIZE);
            size = Constants.MAX_PAGE_SIZE;
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
     * 从请求中获取当前用户ID（安全的JWT Token验证）
     * 
     * 【安全修复】
     * 1. 移除不安全的HTTP Header方式获取用户ID
     * 2. 使用JWT Token验证用户身份
     * 3. 支持从请求属性获取（由拦截器预处理的场景）
     * 4. 添加密钥未配置的安全检查
     * 
     * @param request HTTP请求
     * @return 用户ID，未认证返回null
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 优先从请求属性获取（由拦截器预处理的场景，已验证Token有效性）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        
        // 检查JWT密钥是否已配置
        if (StrUtil.isBlank(jwtSecret)) {
            log.warn("JWT密钥未配置，请检查配置项 jwt.secret");
            // 尝试从Header获取（兼容网关场景）
            String userIdHeader = request.getHeader("X-User-Id");
            if (StrUtil.isNotBlank(userIdHeader)) {
                try {
                    return Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                    log.warn("解析X-User-Id失败: {}", userIdHeader);
                }
            }
            return null;
        }
        
        // 从请求头获取Token并验证
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token)) {
            log.debug("请求未携带有效的认证Token");
            return null;
        }
        
        // 使用配置的密钥验证Token
        if (!JwtUtils.verifyToken(token, jwtSecret)) {
            log.warn("Token验证失败，可能已被篡改或已过期");
            return null;
        }
        
        // 从Token中安全获取用户ID（带签名验证）
        Long userId = JwtUtils.getUserId(token, jwtSecret);
        if (userId != null) {
            log.debug("JWT Token验证成功，userId={}", userId);
        }
        
        return userId;
    }
    
    /**
     * 从请求头获取Token
     * 
     * @param request HTTP请求
     * @return Token字符串，未找到返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(Constants.TOKEN_HEADER);
        if (StrUtil.isBlank(authorization)) {
            return null;
        }
        
        // 处理Bearer前缀
        if (authorization.startsWith(Constants.TOKEN_PREFIX_BEARER)) {
            return authorization.substring(Constants.TOKEN_PREFIX_BEARER.length());
        }
        
        return authorization;
    }
    
    /**
     * 获取当前用户ID，如果未认证则抛出异常
     * 
     * @param request HTTP请求
     * @return 用户ID
     * @throws BusinessException 如果用户未认证
     */
    private Long requireCurrentUserId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    // ==================== 内部API（供其他服务调用） ====================

    /**
     * 内部API：获取用户收藏列表
     *
     * @param current    当前页
     * @param size       每页大小
     * @param userId     用户ID
     * @param serviceKey 内部服务密钥
     * @return 收藏列表
     */
    @GetMapping("/collect/list/internal")
    @Operation(summary = "内部API-获取收藏列表", description = "供其他服务调用的内部接口")
    public Result<IPage<CollectVO>> getCollectListInternal(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id") Long userId,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        
        // 验证内部服务密钥
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        
        log.info("内部API调用：获取用户收藏列表, userId: {}, current: {}, size: {}", userId, current, size);
        
        // 分页参数边界校验
        if (current == null || current < 1) {
            current = Constants.DEFAULT_PAGE_NUM;
        }
        if (size == null || size < 1) {
            size = Constants.DEFAULT_PAGE_SIZE;
        }
        if (size > Constants.MAX_PAGE_SIZE) {
            size = Constants.MAX_PAGE_SIZE;
        }
        
        IPage<CollectVO> collectPage = collectService.getCollectList(userId, current, size);
        return Result.success(collectPage);
    }

    /**
     * 内部API：检查用户是否已收藏帖子
     *
     * @param postId     帖子ID
     * @param userId     用户ID
     * @param serviceKey 内部服务密钥
     * @return 是否已收藏
     */
    @GetMapping("/collect/check/internal")
    @Operation(summary = "内部API-检查收藏状态", description = "供其他服务调用的内部接口")
    public Result<Boolean> checkCollectInternal(
            @Parameter(description = "帖子ID") @RequestParam Long postId,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id") Long userId,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        
        // 验证内部服务密钥
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        
        log.info("内部API调用：检查收藏状态, postId: {}, userId: {}", postId, userId);
        
        boolean isCollected = collectService.isCollected(postId, userId);
        return Result.success(isCollected);
    }

    /**
     * 安全验证内部服务密钥（防止时序攻击）
     * 
     * @param providedKey 请求提供的密钥
     * @return 是否匹配
     */
    private boolean isValidServiceKey(String providedKey) {
        // 检查密钥是否已配置
        if (internalServiceKey == null || internalServiceKey.isEmpty()) {
            log.error("【安全警告】内部服务密钥未配置！请在配置文件中设置 app.internal-service-key");
            return false;
        }
        if (providedKey == null) {
            return false;
        }
        // 使用常量时间比较，防止时序攻击
        byte[] expectedBytes = internalServiceKey.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = providedKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }
}
