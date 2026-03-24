package com.campus.forum.controller;

import com.campus.forum.api.UserServiceClient;
import com.campus.forum.dto.PostCreateDTO;
import com.campus.forum.dto.PostQueryDTO;
import com.campus.forum.dto.PostUpdateDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.Result;
import com.campus.forum.service.PostService;
import com.campus.forum.utils.IpUtils;
import com.campus.forum.vo.PostDetailVO;
import com.campus.forum.vo.PostListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子控制器
 * 提供帖子相关的REST API接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子相关接口")
@Validated
public class PostController {

    private final PostService postService;

    /**
     * 用户服务客户端（用于二次验证）
     * 在关键操作时通过Feign调用用户服务验证用户角色
     */
    private final UserServiceClient userServiceClient;

    /**
     * 内部服务调用密钥
     * 安全警告：生产环境必须在配置文件中设置 app.internal-service-key，不能依赖默认值
     * 
     * 【安全修复】添加默认值为空字符串，防止配置缺失时应用启动失败
     * 同时在验证方法中检查空值并记录错误日志
     */
    @Value("${app.internal-service-key:}")
    private String internalServiceKey;

    /**
     * 安全比较内部服务密钥（防止时序攻击）
     * 使用MessageDigest.isEqual进行常量时间比较，避免通过响应时间推断密钥信息
     *
     * 【安全修复】增加空值检查，防止密钥未配置时被绕过
     *
     * @param providedKey 请求提供的密钥
     * @return 是否匹配
     */
    private boolean isValidServiceKey(String providedKey) {
        // 【安全修复】检查密钥是否已配置
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

    /**
     * 获取帖子列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param forumId  板块ID
     * @param userId   用户ID（查询指定用户的帖子）
     * @param type     帖子类型
     * @param sortType 排序类型
     * @param status   帖子状态
     * @param keyword  搜索关键词
     * @param request  HTTP请求
     * @return 帖子列表
     */
    @GetMapping
    @Operation(summary = "获取帖子列表", description = "分页获取帖子列表，支持多种筛选条件")
    public Result<PageResult<PostListVO>> getPostList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小值为1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小值为1") @Max(value = 100, message = "每页大小最大值为100") Integer size,
            @Parameter(description = "板块ID") @RequestParam(required = false) Long forumId,
            @Parameter(description = "用户ID（查询指定用户的帖子）") @RequestParam(required = false) Long userId,
            @Parameter(description = "帖子类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "排序类型(1-最新发布 2-最新回复 3-热度排序)") @RequestParam(required = false) Integer sortType,
            @Parameter(description = "是否置顶") @RequestParam(required = false) Integer isTop,
            @Parameter(description = "是否精华") @RequestParam(required = false) Integer isEssence,
            @Parameter(description = "帖子状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        log.info("获取帖子列表, current: {}, size: {}, forumId: {}, userId: {}, status: {}", current, size, forumId, userId, status);

        // 构建查询参数
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setForumId(forumId);
        queryDTO.setUserId(userId);
        queryDTO.setType(type);
        queryDTO.setSortType(sortType);
        queryDTO.setIsTop(isTop);
        queryDTO.setIsEssence(isEssence);

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        // 权限校验：只有管理员才能查询非已发布状态的帖子
        boolean isAdmin = isAdmin(request);
        if (status != null && status != 1 && !isAdmin) {
            log.warn("普通用户尝试查询非已发布状态帖子, status: {}, userId: {}", status, currentUserId);
            queryDTO.setStatus(1); // 强制只查询已发布的帖子
        } else {
            // 如果没有传递status参数，默认只查询已发布的帖子
            queryDTO.setStatus(status != null ? status : 1);
        }

        // 查询帖子列表
        PageResult<PostListVO> result;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 有关键词时走搜索逻辑
            result = postService.searchPosts(keyword, queryDTO, currentUserId);
        } else {
            result = postService.getPostList(queryDTO, currentUserId);
        }

        return Result.success(result);
    }

    /**
     * 获取帖子详情
     *
     * @param id      帖子ID
     * @param request HTTP请求
     * @return 帖子详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情", description = "获取指定帖子的详细信息")
    public Result<PostDetailVO> getPostDetail(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {

        log.info("获取帖子详情, postId: {}", id);

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        // 【修复】获取用户IP地址（用于浏览量防刷）
        String ipAddress = IpUtils.getIpAddr(request);

        // 查询帖子详情
        PostDetailVO detail = postService.getPostDetail(id, currentUserId, ipAddress);

        return Result.success(detail);
    }

    /**
     * 发布帖子
     *
     * @param createDTO 帖子创建DTO
     * @param request   HTTP请求
     * @return 帖子ID
     */
    @PostMapping
    @Operation(summary = "发布帖子", description = "发布新帖子")
    public Result<Long> publishPost(
            @Validated @RequestBody PostCreateDTO createDTO,
            HttpServletRequest request) {

        log.info("发布帖子, title: {}", createDTO.getTitle());

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        // 获取IP地址
        String ipAddress = IpUtils.getIpAddr(request);

        // 发布帖子
        Long postId = postService.publishPost(createDTO, userId, ipAddress);

        return Result.success("发布成功", postId);
    }

    /**
     * 编辑帖子
     *
     * @param id        帖子ID
     * @param updateDTO 帖子更新DTO
     * @param request   HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑帖子", description = "编辑指定帖子内容")
    public Result<Boolean> updatePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Validated @RequestBody PostUpdateDTO updateDTO,
            HttpServletRequest request) {

        log.info("编辑帖子, postId: {}", id);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        // 设置帖子ID
        updateDTO.setId(id);

        // 编辑帖子
        boolean result = postService.updatePost(updateDTO, userId);

        return Result.success("编辑成功", result);
    }

    /**
     * 删除帖子
     *
     * @param id      帖子ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子", description = "删除指定帖子（帖子作者或管理员可删除）")
    public Result<Boolean> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {

        log.info("删除帖子, postId: {}", id);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        // 【安全修复】检查是否为管理员 - 使用二次验证防止HTTP Header伪造
        // 对于删除操作，管理员需要二次验证，普通用户删除自己的帖子则不需要
        boolean isAdmin = verifyAdminWithSecondCheck(request);

        // 删除帖子
        boolean result = postService.deletePost(id, userId, isAdmin);

        return Result.success("删除成功", result);
    }

    /**
     * 置顶帖子
     *
     * @param id      帖子ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/top")
    @Operation(summary = "置顶帖子", description = "设置或取消帖子置顶状态（需要管理员权限）")
    public Result<Map<String, Object>> setTop(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "是否置顶(0-取消 1-置顶)") @RequestParam(defaultValue = "1") Integer isTop,
            HttpServletRequest request) {

        log.info("置顶帖子, postId: {}, isTop: {}", id, isTop);

        // 【修复】验证参数有效性
        if (isTop != 0 && isTop != 1) {
            return Result.fail(400, "参数无效，isTop只能是0（取消置顶）或1（置顶）");
        }

        // 获取当前用户ID（需要管理员权限）
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }

        // 【修复】验证管理员权限 - 使用二次验证防止HTTP Header伪造
        if (!verifyAdminWithSecondCheck(request)) {
            log.warn("用户无管理员权限，无法置顶帖子: operatorId={}", operatorId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 【安全修复】将管理员验证结果传递给Service层进行二次校验
        boolean isAdmin = true; // 通过二次验证后确认为管理员

        // 置顶帖子
        boolean result = postService.setTop(id, isTop, operatorId, isAdmin);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isTop", isTop);
        resultMap.put("message", isTop == 1 ? "置顶成功" : "取消置顶成功");

        return Result.success(resultMap);
    }

    /**
     * 加精帖子
     *
     * @param id      帖子ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/essence")
    @Operation(summary = "加精帖子", description = "设置或取消帖子精华状态（需要管理员权限）")
    public Result<Map<String, Object>> setEssence(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "是否精华(0-取消 1-精华)") @RequestParam(defaultValue = "1") Integer isEssence,
            HttpServletRequest request) {

        log.info("加精帖子, postId: {}, isEssence: {}", id, isEssence);

        // 【修复】验证参数有效性
        if (isEssence != 0 && isEssence != 1) {
            return Result.fail(400, "参数无效，isEssence只能是0（取消加精）或1（加精）");
        }

        // 获取当前用户ID（需要管理员权限）
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }

        // 【修复】验证管理员权限 - 使用二次验证防止HTTP Header伪造
        if (!verifyAdminWithSecondCheck(request)) {
            log.warn("用户无管理员权限，无法加精帖子: operatorId={}", operatorId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }
        
        // 【安全修复】将管理员验证结果传递给Service层进行二次校验
        boolean isAdmin = true; // 通过二次验证后确认为管理员

        // 加精帖子
        boolean result = postService.setEssence(id, isEssence, operatorId, isAdmin);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isEssence", isEssence);
        resultMap.put("message", isEssence == 1 ? "加精成功" : "取消加精成功");

        return Result.success(resultMap);
    }

    /**
     * 移动帖子
     *
     * @param id      帖子ID
     * @param forumId 目标版块ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "移动帖子", description = "将帖子移动到其他版块（需要管理员权限）")
    public Result<Boolean> movePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "目标版块ID") @RequestParam Long forumId,
            HttpServletRequest request) {

        log.info("移动帖子, postId: {}, forumId: {}", id, forumId);

        // 获取当前用户ID（需要管理员权限）
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }

        // 验证管理员权限 - 使用二次验证
        if (!verifyAdminWithSecondCheck(request)) {
            log.warn("用户无管理员权限，无法移动帖子: operatorId={}", operatorId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }

        // 移动帖子
        boolean result = postService.movePost(id, forumId, operatorId);

        return Result.success("移动成功", result);
    }

    /**
     * 关闭/打开帖子
     *
     * @param id      帖子ID
     * @param status  帖子状态(0-关闭 1-打开)
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/close")
    @Operation(summary = "关闭帖子", description = "关闭或打开帖子（需要管理员权限）")
    public Result<Boolean> closePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "帖子状态(0-关闭 1-打开)") @RequestParam(defaultValue = "0") Integer status,
            HttpServletRequest request) {

        log.info("关闭帖子, postId: {}, status: {}", id, status);

        // 获取当前用户ID（需要管理员权限）
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }

        // 验证管理员权限 - 使用二次验证
        if (!verifyAdminWithSecondCheck(request)) {
            log.warn("用户无管理员权限，无法关闭帖子: operatorId={}", operatorId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }

        // 关闭/打开帖子
        boolean result = postService.updatePostStatus(id, status, operatorId);

        return Result.success(status == 0 ? "关闭成功" : "打开成功", result);
    }

    /**
     * 审核帖子
     *
     * @param id      帖子ID
     * @param status  审核状态(2-审核通过 3-审核拒绝)
     * @param reason  审核备注（拒绝时必填）
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/{id}/audit")
    @Operation(summary = "审核帖子", description = "审核帖子通过或拒绝（需要管理员权限）")
    public Result<Boolean> auditPost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "审核状态(2-审核通过 3-审核拒绝)") @RequestParam Integer status,
            @Parameter(description = "审核备注") @RequestParam(required = false) String reason,
            HttpServletRequest request) {

        log.info("审核帖子, postId: {}, status: {}, reason: {}", id, status, reason);

        // 获取当前用户ID（需要管理员权限）
        Long operatorId = getCurrentUserId(request);
        if (operatorId == null) {
            return Result.fail(401, "请先登录");
        }

        // 验证管理员权限 - 使用二次验证
        if (!verifyAdminWithSecondCheck(request)) {
            log.warn("用户无管理员权限，无法审核帖子: operatorId={}", operatorId);
            return Result.fail(403, "无权限执行此操作，需要管理员权限");
        }

        // 验证状态值
        if (status != 2 && status != 3) {
            return Result.fail(400, "审核状态无效，只能为2（通过）或3（拒绝）");
        }

        // 拒绝时需要填写原因
        if (status == 3 && (reason == null || reason.trim().isEmpty())) {
            return Result.fail(400, "拒绝审核时必须填写原因");
        }

        // 审核帖子
        boolean result = postService.auditPost(id, status, reason, operatorId);

        return Result.success(status == 2 ? "审核通过" : "审核拒绝", result);
    }

    /**
     * 获取热门帖子
     *
     * @param limit   数量限制
     * @param request HTTP请求
     * @return 热门帖子列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门帖子", description = "获取热门帖子列表")
    public Result<List<PostListVO>> getHotPosts(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") @Min(value = 1, message = "数量限制最小值为1") @Max(value = 50, message = "数量限制最大值为50") Integer limit,
            HttpServletRequest request) {

        log.info("获取热门帖子, limit: {}", limit);

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        // 查询热门帖子
        List<PostListVO> hotPosts = postService.getHotPosts(limit, currentUserId);

        return Result.success(hotPosts);
    }

    /**
     * 获取版块帖子列表
     *
     * @param forumId  版块ID
     * @param current  当前页
     * @param size     每页大小
     * @param request  HTTP请求
     * @return 帖子列表
     */
    @GetMapping("/forum/{forumId}")
    @Operation(summary = "获取版块帖子列表", description = "分页获取指定版块的帖子列表")
    public Result<PageResult<PostListVO>> getPostsByForum(
            @Parameter(description = "版块ID") @PathVariable Long forumId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小值为1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小值为1") @Max(value = 100, message = "每页大小最大值为100") Integer size,
            HttpServletRequest request) {

        log.info("获取版块帖子列表, forumId: {}, current: {}, size: {}", forumId, current, size);

        // 构建查询参数
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setForumId(forumId);
        queryDTO.setStatus(1); // 只查询已发布的帖子

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        // 查询帖子列表
        PageResult<PostListVO> result = postService.getPostList(queryDTO, currentUserId);

        return Result.success(result);
    }

    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @param current 当前页
     * @param size    每页大小
     * @param request HTTP请求
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子")
    public Result<PageResult<PostListVO>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小值为1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小值为1") @Max(value = 100, message = "每页大小最大值为100") Integer size,
            @Parameter(description = "板块ID") @RequestParam(required = false) Long forumId,
            HttpServletRequest request) {

        log.info("搜索帖子, keyword: {}, current: {}, size: {}", keyword, current, size);

        // 构建查询参数
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setForumId(forumId);
        queryDTO.setStatus(1);

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        // 搜索帖子
        PageResult<PostListVO> result = postService.searchPosts(keyword, queryDTO, currentUserId);

        return Result.success(result);
    }

    /**
     * 点赞帖子
     *
     * @param id      帖子ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞帖子", description = "点赞或取消点赞帖子")
    public Result<Map<String, Object>> likePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {

        log.info("点赞帖子, postId: {}", id);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        // 点赞帖子
        boolean isLike = postService.likePost(id, userId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isLike", isLike);
        resultMap.put("message", isLike ? "点赞成功" : "已取消点赞");

        return Result.success(resultMap);
    }

    /**
     * 收藏帖子
     *
     * @param id      帖子ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏帖子", description = "收藏或取消收藏帖子")
    public Result<Map<String, Object>> collectPost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {

        log.info("收藏帖子, postId: {}", id);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        // 收藏帖子
        boolean isCollect = postService.collectPost(id, userId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isCollect", isCollect);
        resultMap.put("message", isCollect ? "收藏成功" : "已取消收藏");

        return Result.success(resultMap);
    }

    /**
     * 获取用户帖子列表
     *
     * @param userId  用户ID
     * @param current 当前页
     * @param size    每页大小
     * @param request HTTP请求
     * @return 帖子列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户帖子列表", description = "获取指定用户发布的帖子列表")
    public Result<PageResult<PostListVO>> getUserPosts(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小值为1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小值为1") @Max(value = 100, message = "每页大小最大值为100") Integer size,
            HttpServletRequest request) {

        log.info("获取用户帖子列表, userId: {}", userId);

        // 构建查询参数
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setStatus(1);

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(request);

        // 查询用户帖子
        PageResult<PostListVO> result = postService.getUserPosts(userId, queryDTO, currentUserId);

        return Result.success(result);
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

    /**
     * 检查当前用户是否为管理员（快速检查，基于HTTP Header）
     *
     * 【安全说明 - 重要】
     * 本方法的管理员权限验证依赖于HTTP Header中的X-User-Role字段。
     * 在微服务架构中，该Header由API网关层（forum-gateway）解析JWT Token后设置，
     * 并非直接来自客户端请求。
     *
     * 【安全风险】
     * 如果攻击者能够绕过网关直接访问微服务端口，可以伪造X-User-Role头
     * 获取管理员权限。因此，本方法仅用于非关键操作的快速权限检查。
     *
     * 【缓解措施】
     * 1. 所有外部请求必须经过API网关
     * 2. 服务间网络隔离，外部无法直接访问微服务端口
     * 3. 网关层正确实现了JWT Token验证和角色提取逻辑
     * 4. 关键操作（置顶、加精、删除等）使用 verifyAdminWithSecondCheck 进行二次验证
     *
     * @param request HTTP请求
     * @return true-管理员，false-非管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 从请求头获取用户角色（由网关解析JWT后传递）
        // 注意：X-User-Role由网关层解析JWT Token后设置，请确保请求经过网关验证
        String role = request.getHeader("X-User-Role");
        if (role != null) {
            // 检查是否为管理员角色（ADMIN或ROLE_ADMIN）
            return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        }
        return false;
    }

    /**
     * 验证用户是否为管理员（带二次验证）
     *
     * 【安全说明】
     * 此方法用于关键操作（置顶、加精、删除等）的权限验证。
     * 除了检查HTTP Header外，还会通过Feign调用用户服务进行二次验证，
     * 确保用户角色的真实性，防止HTTP Header伪造攻击。
     *
     * 【二次验证流程】
     * 1. 首先检查HTTP Header中的角色（快速检查）
     * 2. 如果Header显示为管理员，则调用用户服务验证真实角色
     * 3. 只有两步验证都通过才返回true
     *
     * 【降级策略】
     * 如果用户服务不可用，会记录警告日志并返回false（安全优先）
     *
     * @param request HTTP请求
     * @return true-管理员，false-非管理员
     */
    private boolean verifyAdminWithSecondCheck(HttpServletRequest request) {
        // 第一步：快速检查HTTP Header
        if (!isAdmin(request)) {
            return false;
        }

        // 第二步：通过用户服务进行二次验证
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            log.warn("二次验证失败：无法获取用户ID");
            return false;
        }

        try {
            // 调用用户服务验证管理员角色
            Result<Boolean> result = userServiceClient.verifyAdmin(userId, internalServiceKey);
            if (result != null && result.isSuccess() && Boolean.TRUE.equals(result.getData())) {
                log.info("管理员二次验证成功, userId: {}", userId);
                return true;
            } else {
                log.warn("管理员二次验证失败：角色不匹配, userId: {}, result: {}", userId, result);
                return false;
            }
        } catch (Exception e) {
            // 用户服务不可用时，采用安全优先策略：拒绝操作
            log.error("管理员二次验证失败：用户服务不可用, userId: {}", userId, e);
            return false;
        }
    }

    // ==================== 内部API（供其他服务调用） ====================

    /**
     * 内部API：根据ID获取帖子信息
     *
     * @param id 帖子ID
     * @param serviceKey 内部服务密钥
     * @return 帖子信息
     */
    @GetMapping("/internal/{id}")
    @Operation(summary = "内部API-获取帖子信息", description = "供其他服务调用的内部接口")
    public Result<PostListVO> getPostByIdInternal(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        
        log.info("内部API调用：获取帖子信息, postId: {}", id);

        // 查询帖子（内部API调用，不记录浏览量防刷）
        PostDetailVO detail = postService.getPostDetail(id, null, null);

        // 转换为简化VO
        PostListVO listVO = new PostListVO();
        listVO.setId(detail.getId());
        listVO.setTitle(detail.getTitle());
        listVO.setUserId(detail.getUserId());
        listVO.setUserName(detail.getUserName());
        listVO.setUserAvatar(detail.getUserAvatar());
        listVO.setForumId(detail.getForumId());
        listVO.setForumName(detail.getForumName());
        listVO.setSummary(detail.getSummary());
        listVO.setViewCount(detail.getViewCount());
        listVO.setLikeCount(detail.getLikeCount());
        listVO.setCommentCount(detail.getCommentCount());
        listVO.setCollectCount(detail.getCollectCount());
        listVO.setIsTop(detail.getIsTop());
        listVO.setIsEssence(detail.getIsEssence());
        listVO.setStatus(detail.getStatus());
        listVO.setCreateTime(detail.getCreateTime());

        return Result.success(listVO);
    }

    /**
     * 内部API：更新帖子统计
     *
     * @param id    帖子ID
     * @param field 统计字段
     * @param delta 变化量
     * @param serviceKey 内部服务密钥
     * @return 操作结果
     */
    @PostMapping("/internal/{id}/stats")
    @Operation(summary = "内部API-更新帖子统计", description = "供其他服务调用的内部接口")
    public Result<Boolean> updatePostStatsInternal(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "统计字段") @RequestParam String field,
            @Parameter(description = "变化量") @RequestParam int delta,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        
        log.info("内部API调用：更新帖子统计, postId: {}, field: {}, delta: {}", id, field, delta);

        // 根据字段类型更新
        switch (field) {
            case "viewCount":
                postService.incrementViewCount(id);
                break;
            case "commentCount":
                postService.updateCommentCount(id, delta);
                break;
            default:
                return Result.fail(400, "未知的统计字段: " + field);
        }

        return Result.success(true);
    }

    /**
     * 内部API：获取用户帖子列表
     *
     * @param userId 用户ID
     * @param page   当前页
     * @param size   每页大小
     * @param serviceKey 内部服务密钥
     * @return 帖子列表
     */
    @GetMapping("/internal/user/{userId}")
    @Operation(summary = "内部API-获取用户帖子列表", description = "供其他服务调用的内部接口")
    public Result<PageResult<PostListVO>> getPostsByUserIdInternal(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "内部服务密钥") @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        // 验证内部服务密钥（使用常量时间比较，防止时序攻击）
        if (!isValidServiceKey(serviceKey)) {
            log.warn("内部API调用鉴权失败，serviceKey: {}", serviceKey);
            return Result.fail(403, "无权限访问内部API");
        }
        
        log.info("内部API调用：获取用户帖子列表, userId: {}, page: {}, size: {}", userId, page, size);

        // 构建查询参数
        PostQueryDTO queryDTO = new PostQueryDTO();
        queryDTO.setCurrent(page);
        queryDTO.setSize(size);
        queryDTO.setStatus(1);

        // 查询用户帖子
        PageResult<PostListVO> result = postService.getUserPosts(userId, queryDTO, null);

        return Result.success(result);
    }
}
