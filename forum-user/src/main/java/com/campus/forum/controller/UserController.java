package com.campus.forum.controller;

import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.post.PostDTO;
import com.campus.forum.api.comment.CommentApi;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * @param id     用户ID
     * @param status 状态（0-禁用，1-正常）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "管理员启用/禁用用户账户")
    public Result<Boolean> updateUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态（0-禁用，1-正常）", required = true) @RequestParam Integer status) {
        log.info("更新用户状态，用户ID：{}，状态：{}", id, status);
        
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
     * @param id       用户ID
     * @param queryDTO 查询条件
     * @return 粉丝列表
     */
    @GetMapping("/{id}/followers")
    @Operation(summary = "获取粉丝列表", description = "分页获取用户的粉丝列表")
    public Result<PageResult<UserFollowVO>> getFollowers(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "查询条件") UserQueryDTO queryDTO) {
        log.info("获取粉丝列表，用户ID：{}", id);
        PageResult<UserFollowVO> result = userFollowService.getFollowers(id, queryDTO);
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
            var result = postApi.getPostsByUserId(id, queryDTO.getCurrent(), queryDTO.getSize());
            if (result != null && result.getData() != null) {
                return Result.success(result.getData());
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
        if (!id.equals(currentUserId)) {
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
}
