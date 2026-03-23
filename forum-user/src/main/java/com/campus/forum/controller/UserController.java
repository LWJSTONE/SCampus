package com.campus.forum.controller;

import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.post.PostDTO;
import com.campus.forum.api.comment.CommentApi;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.dto.PasswordUpdateDTO;
import com.campus.forum.dto.UserQueryDTO;
import com.campus.forum.dto.UserUpdateDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.Result;
import com.campus.forum.service.UserFollowService;
import com.campus.forum.service.UserService;
import com.campus.forum.vo.UserDetailVO;
import com.campus.forum.vo.UserFollowVO;
import com.campus.forum.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * 用户控制器
 * 
 * 提供用户相关的REST API接口：
 * - 用户信息查询（列表、详情）
 * - 用户信息更新（基本信息、头像、密码）
 * - 用户关注功能（关注、取消关注、粉丝列表、关注列表）
 * - 用户关联数据查询（帖子、评论、收藏）
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口，包括用户信息管理、关注功能等")
public class UserController {

    private final UserService userService;
    private final UserFollowService userFollowService;
    private final PostApi postApi;
    private final CommentApi commentApi;

    /**
     * 内部服务调用密钥
     */
    @Value("${app.internal-service-key:campus-internal-service-key-2024}")
    private String internalServiceKey;

    /**
     * 安全比较内部服务密钥（防止时序攻击）
     * 使用MessageDigest.isEqual进行常量时间比较，避免通过响应时间推断密钥信息
     *
     * @param providedKey 请求提供的密钥
     * @return 是否匹配
     */
    private boolean isValidServiceKey(String providedKey) {
        if (internalServiceKey == null || providedKey == null) {
            return false;
        }
        // 使用常量时间比较，防止时序攻击
        byte[] expectedBytes = internalServiceKey.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = providedKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }

    // ==================== 用户信息管理 ====================

    /**
     * 获取用户列表
     * 
     * 支持分页查询和关键词搜索
     *
     * @param queryDTO 查询条件
     * @return 用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持关键词搜索")
    public Result<PageResult<UserListVO>> getUserList(
            @Parameter(description = "查询条件") UserQueryDTO queryDTO) {
        log.info("获取用户列表，条件：{}", queryDTO);
        PageResult<UserListVO> result = userService.getUserList(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public Result<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("获取用户详情，用户ID：{}", id);
        UserDetailVO detail = userService.getUserDetail(id, currentUserId);
        return Result.success(detail);
    }

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新用户基本信息，如昵称、邮箱、手机号、简介等")
    public Result<Boolean> updateUserInfo(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "更新信息", required = true) @Validated @RequestBody UserUpdateDTO updateDTO,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = true) Long currentUserId) {
        log.info("更新用户信息，用户ID：{}", id);
        
        // 验证权限：只能修改自己的信息
        if (!id.equals(currentUserId)) {
            return Result.fail(403, "无权限修改其他用户信息");
        }
        
        boolean result = userService.updateUserInfo(id, updateDTO);
        return Result.success(result);
    }

    /**
     * 更新用户头像
     *
     * @param id      用户ID
     * @param request 包含头像URL的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/avatar")
    @Operation(summary = "更新用户头像", description = "更新用户头像URL")
    public Result<Boolean> updateAvatar(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "头像URL", required = true) @RequestBody Map<String, String> request,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = true) Long currentUserId) {
        log.info("更新用户头像，用户ID：{}", id);
        
        // 验证权限：只能修改自己的头像
        if (!id.equals(currentUserId)) {
            return Result.fail(403, "无权限修改其他用户头像");
        }
        
        String avatar = request.get("avatar");
        if (avatar == null || avatar.isEmpty()) {
            return Result.fail(400, "头像URL不能为空");
        }
        
        // 校验头像URL安全性
        String validationError = validateAvatarUrl(avatar);
        if (validationError != null) {
            return Result.fail(400, validationError);
        }
        
        boolean result = userService.updateAvatar(id, avatar);
        return Result.success(result);
    }

    /**
     * 修改密码
     *
     * @param id           用户ID
     * @param passwordDTO 密码更新信息
     * @return 操作结果
     */
    @PutMapping("/{id}/password")
    @Operation(summary = "修改密码", description = "修改用户密码，需要验证原密码")
    public Result<Boolean> updatePassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "密码更新信息", required = true) @Validated @RequestBody PasswordUpdateDTO passwordDTO,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = true) Long currentUserId) {
        log.info("修改密码，用户ID：{}", id);
        
        // 验证权限：只能修改自己的密码
        if (!id.equals(currentUserId)) {
            return Result.fail(403, "无权限修改其他用户密码");
        }
        
        boolean result = userService.updatePassword(id, passwordDTO);
        return Result.success(result);
    }

    /**
     * 更新用户状态（管理员）
     *
     * 【安全说明】
     * 本接口的管理员权限验证依赖于HTTP Header中的X-User-Role字段。
     * 在微服务架构中，该Header由API网关层（forum-gateway）解析JWT Token后设置，
     * 并非直接来自客户端请求。
     * 
     * 网关层通过AuthGlobalFilter解析JWT Token，验证用户身份和角色后，
     * 将用户ID（X-User-Id）和角色（X-User-Role）传递给下游微服务。
     * 
     * 因此，此接口的安全性依赖于以下前提条件：
     * 1. 所有外部请求必须经过API网关
     * 2. 服务间网络隔离，外部无法直接访问微服务端口
     * 3. 网关层正确实现了JWT Token验证和角色提取逻辑
     *
     * 如果直接调用此服务（绕过网关），需要确保请求来源可信。
     * 生产环境建议在网络层面限制只有网关可以访问此服务的API端口。
     *
     * @param id     用户ID
     * @param status 状态（0-禁用，1-正常）
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "管理员启用/禁用用户账户")
    public Result<Boolean> updateUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态（0-禁用，1-正常）", required = true) @RequestParam Integer status,
            HttpServletRequest request) {
        log.info("更新用户状态，用户ID：{}，状态：{}", id, status);
        
        // 验证管理员权限
        // 注意：X-User-Role由网关层解析JWT Token后设置，请确保请求经过网关验证
        String role = request.getHeader("X-User-Role");
        if (role == null || (!"ADMIN".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role))) {
            log.warn("非管理员尝试更新用户状态，角色：{}", role);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 验证状态值
        if (status != 0 && status != 1) {
            return Result.fail(400, "状态值无效，只能为0（禁用）或1（正常）");
        }
        
        boolean result = userService.updateStatus(id, status);
        return Result.success(result);
    }

    // ==================== 关注功能 ====================

    /**
     * 获取粉丝列表
     *
     * @param id            用户ID
     * @param queryDTO      查询条件
     * @param currentUserId 当前登录用户ID（用于判断当前用户是否关注了粉丝）
     * @return 粉丝列表
     */
    @GetMapping("/{id}/followers")
    @Operation(summary = "获取粉丝列表", description = "分页获取用户的粉丝列表")
    public Result<PageResult<UserFollowVO>> getFollowers(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "查询条件") UserQueryDTO queryDTO,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("获取粉丝列表，用户ID：{}，当前登录用户ID：{}", id, currentUserId);
        PageResult<UserFollowVO> result = userFollowService.getFollowers(id, queryDTO, currentUserId);
        return Result.success(result);
    }

    /**
     * 获取关注列表
     *
     * @param id       用户ID
     * @param queryDTO 查询条件
     * @return 关注列表
     */
    @GetMapping("/{id}/following")
    @Operation(summary = "获取关注列表", description = "分页获取用户的关注列表")
    public Result<PageResult<UserFollowVO>> getFollowing(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "查询条件") UserQueryDTO queryDTO) {
        log.info("获取关注列表，用户ID：{}", id);
        PageResult<UserFollowVO> result = userFollowService.getFollowing(id, queryDTO);
        return Result.success(result);
    }

    /**
     * 关注用户
     *
     * @param id            被关注的用户ID
     * @param currentUserId 当前登录用户ID
     * @return 操作结果
     */
    @PostMapping("/{id}/follow")
    @Operation(summary = "关注用户", description = "关注指定用户")
    public Result<Boolean> follow(
            @Parameter(description = "被关注的用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "当前登录用户ID", required = true) @RequestHeader(value = "X-User-Id") Long currentUserId) {
        log.info("关注用户，当前用户：{}，被关注用户：{}", currentUserId, id);
        boolean result = userFollowService.follow(currentUserId, id);
        return Result.success(result);
    }

    /**
     * 取消关注
     *
     * @param id            被取消关注的用户ID
     * @param currentUserId 当前登录用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}/follow")
    @Operation(summary = "取消关注", description = "取消关注指定用户")
    public Result<Boolean> unfollow(
            @Parameter(description = "被取消关注的用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "当前登录用户ID", required = true) @RequestHeader(value = "X-User-Id") Long currentUserId) {
        log.info("取消关注，当前用户：{}，被取消关注用户：{}", currentUserId, id);
        boolean result = userFollowService.unfollow(currentUserId, id);
        return Result.success(result);
    }

    // ==================== 用户关联数据 ====================

    /**
     * 获取用户帖子
     *
     * @param id       用户ID
     * @param queryDTO 查询条件
     * @return 帖子列表
     */
    @GetMapping("/{id}/posts")
    @Operation(summary = "获取用户帖子", description = "分页获取用户发布的帖子列表")
    public Result<PageResult<?>> getUserPosts(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "查询条件") UserQueryDTO queryDTO) {
        log.info("获取用户帖子，用户ID：{}", id);
        // 通过Feign调用帖子服务获取用户帖子
        try {
            Result<Page<PostDTO>> result = postApi.getPostsByUserId(id, queryDTO.getCurrent(), queryDTO.getSize());
            if (result != null && result.getData() != null) {
                return Result.success(PageResult.of(result.getData()));
            }
        } catch (Exception e) {
            log.error("调用帖子服务失败", e);
        }
        return Result.success(new PageResult<>());
    }

    /**
     * 获取用户评论
     *
     * @param id       用户ID
     * @param queryDTO 查询条件
     * @return 评论列表
     */
    @GetMapping("/{id}/comments")
    @Operation(summary = "获取用户评论", description = "分页获取用户发表的评论列表")
    public Result<PageResult<?>> getUserComments(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "查询条件") UserQueryDTO queryDTO) {
        log.info("获取用户评论，用户ID：{}", id);
        // 通过Feign调用评论服务获取用户评论
        // 这里返回空列表作为示例，实际需要调用CommentApi
        return Result.success(new PageResult<>());
    }

    /**
     * 获取用户收藏
     *
     * @param id       用户ID
     * @param queryDTO 查询条件
     * @return 收藏列表
     */
    @GetMapping("/{id}/collections")
    @Operation(summary = "获取用户收藏", description = "分页获取用户的收藏列表")
    public Result<PageResult<?>> getUserCollections(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "查询条件") UserQueryDTO queryDTO,
            @Parameter(description = "当前登录用户ID") @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        log.info("获取用户收藏，用户ID：{}", id);
        
        // 只能查看自己的收藏
        if (currentUserId == null || !id.equals(currentUserId)) {
            return Result.fail(403, "无权限查看其他用户收藏");
        }
        
        // 通过Feign调用交互服务获取用户收藏
        // 这里返回空列表作为示例，实际需要调用InteractionApi
        return Result.success(new PageResult<>());
    }

    // ==================== 当前用户 ====================

    /**
     * 获取当前登录用户信息
     *
     * @param currentUserId 当前登录用户ID
     * @return 用户详情
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserDetailVO> getCurrentUser(
            @Parameter(description = "当前登录用户ID", required = true) @RequestHeader(value = "X-User-Id") Long currentUserId) {
        log.info("获取当前用户信息，用户ID：{}", currentUserId);
        UserDetailVO detail = userService.getCurrentUser(currentUserId);
        return Result.success(detail);
    }

    // ==================== 内部API（供其他服务调用） ====================

    /**
     * 内部API：增加用户帖子数
     *
     * @param id 用户ID
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PutMapping("/api/internal/user/{id}/post-count/increment")
    @Operation(summary = "内部API-增加帖子数", description = "供其他服务调用的内部接口")
    public Result<Boolean> incrementPostCount(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        log.info("内部API调用：增加用户帖子数，用户ID: {}", id);
        userService.incrementPostCount(id);
        return Result.success(true);
    }

    /**
     * 内部API：减少用户帖子数
     *
     * @param id 用户ID
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PutMapping("/api/internal/user/{id}/post-count/decrement")
    @Operation(summary = "内部API-减少帖子数", description = "供其他服务调用的内部接口")
    public Result<Boolean> decrementPostCount(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        log.info("内部API调用：减少用户帖子数，用户ID: {}", id);
        userService.decrementPostCount(id);
        return Result.success(true);
    }

    /**
     * 内部API：增加用户评论数
     *
     * @param id 用户ID
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PutMapping("/api/internal/user/{id}/comment-count/increment")
    @Operation(summary = "内部API-增加评论数", description = "供其他服务调用的内部接口")
    public Result<Boolean> incrementCommentCount(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        log.info("内部API调用：增加用户评论数，用户ID: {}", id);
        userService.incrementCommentCount(id);
        return Result.success(true);
    }

    /**
     * 内部API：减少用户评论数
     *
     * @param id 用户ID
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PutMapping("/api/internal/user/{id}/comment-count/decrement")
    @Operation(summary = "内部API-减少评论数", description = "供其他服务调用的内部接口")
    public Result<Boolean> decrementCommentCount(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        log.info("内部API调用：减少用户评论数，用户ID: {}", id);
        userService.decrementCommentCount(id);
        return Result.success(true);
    }

    /**
     * 内部API：封禁用户账号
     *
     * @param id 用户ID
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PutMapping("/api/internal/user/{id}/ban")
    @Operation(summary = "内部API-封禁用户", description = "供其他服务调用的内部接口，封禁用户账号")
    public Result<Boolean> banUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        log.info("内部API调用：封禁用户，用户ID: {}", id);
        boolean result = userService.updateStatus(id, 0); // 0表示禁用/封禁
        return Result.success(result);
    }

    /**
     * 内部API：解禁用户账号
     *
     * @param id 用户ID
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PutMapping("/api/internal/user/{id}/unban")
    @Operation(summary = "内部API-解禁用户", description = "供其他服务调用的内部接口，解禁用户账号")
    public Result<Boolean> unbanUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        log.info("内部API调用：解禁用户，用户ID: {}", id);
        boolean result = userService.updateStatus(id, 1); // 1表示正常
        return Result.success(result);
    }
    
    /**
     * 校验头像URL安全性
     * 禁止javascript协议、data协议等危险URL，只允许http和https协议
     *
     * @param avatarUrl 头像URL
     * @return 校验错误信息，如果校验通过则返回null
     */
    private String validateAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return "头像URL不能为空";
        }
        
        String trimmedUrl = avatarUrl.trim().toLowerCase();
        
        // 禁止危险协议
        if (trimmedUrl.startsWith("javascript:")) {
            return "不允许使用javascript协议的URL";
        }
        if (trimmedUrl.startsWith("data:")) {
            return "不允许使用data协议的URL";
        }
        if (trimmedUrl.startsWith("vbscript:")) {
            return "不允许使用vbscript协议的URL";
        }
        if (trimmedUrl.startsWith("file:")) {
            return "不允许使用file协议的URL";
        }
        
        // 解析URL并验证协议
        try {
            URI uri = new URI(avatarUrl);
            String scheme = uri.getScheme();
            
            // 只允许http和https协议
            if (scheme != null && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                return "只允许使用http或https协议的URL";
            }
            
            // 检查URL中是否包含javascript关键字（防止绕过）
            String urlLower = avatarUrl.toLowerCase();
            if (urlLower.contains("javascript:") || urlLower.contains("vbscript:")) {
                return "URL中包含不允许的脚本协议";
            }
            
        } catch (Exception e) {
            return "URL格式无效";
        }
        
        return null;
    }
}
