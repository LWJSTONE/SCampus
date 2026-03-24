package com.campus.forum.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.api.notify.NotifyApi;
import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.post.PostDTO;
import com.campus.forum.api.user.UserApi;
import com.campus.forum.api.user.UserDTO;
import com.campus.forum.entity.Result;
import com.campus.forum.config.CommentConfig;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.CommentCreateDTO;
import com.campus.forum.dto.CommentQueryDTO;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.CommentLike;
import com.campus.forum.entity.PageResult;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.CommentLikeMapper;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.service.CommentService;
import com.campus.forum.utils.IpUtils;
import com.campus.forum.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * 
 * 实现评论相关的业务逻辑
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final CommentConfig commentConfig;
    private final StringRedisTemplate redisTemplate;

    // 远程服务调用
    private final PostApi postApi;
    private final UserApi userApi;

    // Redis Key前缀
    private static final String REDIS_KEY_COMMENT_COUNT = "comment:count:";
    private static final String REDIS_KEY_COMMENT_LIKE = "comment:like:";
    private static final String REDIS_KEY_USER_COMMENTS = "comment:user:";
    private static final String REDIS_KEY_COMMENT_LIKE_LOCK = "comment:like:lock:";
    private static final long LOCK_EXPIRE_TIME = 3; // 锁过期时间（秒）
    
    // 敏感词列表（实际项目中应从数据库或配置中心获取）
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "敏感词1", "敏感词2", "违禁词", "广告"
    ));

    /**
     * 获取帖子的评论列表
     */
    @Override
    public IPage<CommentVO> getCommentsByPostId(Long postId, CommentQueryDTO queryDTO, Long currentUserId) {
        log.info("获取帖子评论列表, postId: {}, currentUserId: {}", postId, currentUserId);
        
        // 构建分页参数
        Page<CommentVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 查询一级评论
        IPage<CommentVO> commentPage = commentMapper.selectCommentsByPostId(page, postId, currentUserId);
        
        // 处理每条评论
        for (CommentVO comment : commentPage.getRecords()) {
            processComment(comment, currentUserId);
            
            // 加载前几条回复
            if (comment.getReplyCount() != null && comment.getReplyCount() > 0) {
                List<CommentVO> replies = commentMapper.selectRepliesByParentId(
                        comment.getId(), 
                        currentUserId, 
                        Math.min(comment.getReplyCount(), 3) // 默认加载3条回复
                );
                replies.forEach(reply -> processComment(reply, currentUserId));
                comment.setReplies(replies);
                comment.setHasMoreReplies(comment.getReplyCount() > 3);
            }
        }
        
        return commentPage;
    }

    /**
     * 发布评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(CommentCreateDTO createDTO, Long userId, String ipAddress) {
        log.info("发布评论, userId: {}, postId: {}", userId, createDTO.getPostId());
        
        // 0. 防重复提交检查（使用Redis实现幂等性）
        String duplicateKey = "comment:create:" + userId + ":" + createDTO.getPostId() + ":" + createDTO.getContent().hashCode();
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(duplicateKey, "1", 10, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(isFirst)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请勿重复提交评论");
        }
        
        // 1. 参数校验
        validateComment(createDTO);
        
        // 2. 敏感词过滤
        String content = filterSensitiveWords(createDTO.getContent());
        
        // 3. 构建评论实体
        Comment comment = new Comment();
        comment.setPostId(createDTO.getPostId());
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setStatus(1);  // 正常状态：0-已删除，1-正常，2-被系统屏蔽
        comment.setIsHot(0);   // 非热门
        comment.setAuditStatus(1);  // 审核通过（实际项目可能需要审核流程）
        comment.setIpAddress(ipAddress);
        
        // 解析IP归属地
        String ipLocation = IpUtils.getIpLocation(ipAddress);
        comment.setIpLocation(ipLocation);
        
        // 4. 处理父评论和回复用户（只允许一级嵌套）
        Long originalParentId = createDTO.getParentId();  // 保存原始parentId用于后续判断
        if (originalParentId != null && originalParentId > 0) {
            // 验证父评论是否存在
            Comment parentComment = commentMapper.selectById(originalParentId);
            if (parentComment == null || parentComment.getDeleteFlag() == 1) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND, "父评论不存在或已被删除");
            }
            // 检查父评论状态是否正常（status: 0-已删除, 1-正常, 2-被系统屏蔽）
            // 只允许回复status=1的正常评论
            if (parentComment.getStatus() == null || parentComment.getStatus() != 1) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND, "无法回复已删除或被屏蔽的评论");
            }
            
            // 只允许一级嵌套：如果父评论已经是子评论，则使用其父评论ID
            Long finalParentId = originalParentId;
            if (parentComment.getParentId() != null && parentComment.getParentId() > 0) {
                finalParentId = parentComment.getParentId();
                // 更新回复用户为一级评论的作者或指定的回复用户
                if (createDTO.getReplyToUserId() == null) {
                    createDTO.setReplyToUserId(parentComment.getUserId());
                }
            }
            comment.setParentId(finalParentId);
            comment.setReplyToUserId(createDTO.getReplyToUserId() != null ? 
                    createDTO.getReplyToUserId() : parentComment.getUserId());
        } else {
            comment.setParentId(0L);
        }
        
        // 5. 保存评论
        commentMapper.insert(comment);
        
        // 6. 更新父评论的回复数（使用评论最终设置的parentId）
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.incrementReplyCount(comment.getParentId());
        }
        
        // 7. 更新帖子评论数（调用帖子服务或更新缓存）
        updatePostCommentCount(createDTO.getPostId(), 1);
        
        // 8. 发送通知（@用户通知）
        sendNotification(comment, createDTO.getReplyUserName());
        
        log.info("评论发布成功, commentId: {}", comment.getId());
        return comment.getId();
    }

    /**
     * 删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, Long userId) {
        log.info("删除评论, commentId: {}, userId: {}", commentId, userId);
        
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 2. 权限校验（评论作者或管理员可以删除）
        boolean isAuthor = comment.getUserId().equals(userId);
        boolean isAdmin = false;
        
        // 如果不是作者，检查是否是管理员
        if (!isAuthor) {
            try {
                Result<UserDTO> userResult = userApi.getUserById(userId);
                if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                    String role = userResult.getData().getRole();
                    isAdmin = "ADMIN".equals(role);
                }
            } catch (Exception e) {
                log.warn("获取用户角色信息失败, userId: {}", userId, e);
            }
        }
        
        if (!isAuthor && !isAdmin) {
            throw new BusinessException(ResultCode.COMMENT_NO_PERMISSION);
        }
        
        // 3. 获取子评论数量
        int subCommentCount = 0;
        if (comment.getReplyCount() != null && comment.getReplyCount() > 0) {
            subCommentCount = comment.getReplyCount();
            
            // 4. 逻辑删除所有子评论
            commentMapper.deleteByParentId(commentId);
            log.info("删除子评论, parentId: {}, count: {}", commentId, subCommentCount);
        }
        
        // 5. 逻辑删除当前评论
        comment.setDeleteFlag(1);
        comment.setStatus(0);  // 设置为已删除状态：0-已删除，1-正常，2-被系统屏蔽
        commentMapper.updateById(comment);
        
        // 6. 更新父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
        
        // 7. 更新帖子评论数（包括子评论）
        updatePostCommentCount(comment.getPostId(), -1 - subCommentCount);
        
        log.info("评论删除成功, commentId: {}, subCommentCount: {}", commentId, subCommentCount);
        return true;
    }

    /**
     * 点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        log.info("点赞评论, commentId: {}, userId: {}", commentId, userId);
        
        // 1. 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 2. 使用分布式锁防止并发问题
        String lockKey = REDIS_KEY_COMMENT_LIKE_LOCK + commentId + ":" + userId;
        
        try {
            // 尝试获取锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "操作过于频繁，请稍后再试");
            }
            
            // 3. 查询是否已点赞
            CommentLike existingLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
            
            boolean isLike;
            if (existingLike != null) {
                // 已点赞，执行取消点赞
                existingLike.setDeleteFlag(1);
                commentLikeMapper.updateById(existingLike);
                commentMapper.decrementLikeCount(commentId);
                isLike = false;
            } else {
                // 查询是否存在已取消的点赞记录
                CommentLike existRecord = commentLikeMapper.selectExistsRecord(commentId, userId);
                if (existRecord != null) {
                    // 恢复点赞记录
                    existRecord.setDeleteFlag(0);
                    commentLikeMapper.updateById(existRecord);
                } else {
                    // 创建新的点赞记录
                    CommentLike newLike = new CommentLike();
                    newLike.setCommentId(commentId);
                    newLike.setUserId(userId);
                    commentLikeMapper.insert(newLike);
                }
                commentMapper.incrementLikeCount(commentId);
                isLike = true;
            }
            
            // 4. 更新热门状态
            Comment updatedComment = commentMapper.selectById(commentId);
            if (updatedComment.getLikeCount() >= commentConfig.getHotCommentThreshold()) {
                commentMapper.updateHotStatus(commentId, 1);
            } else {
                commentMapper.updateHotStatus(commentId, 0);
            }
            
            // 5. 更新缓存
            String cacheKey = REDIS_KEY_COMMENT_LIKE + commentId + ":" + userId;
            if (isLike) {
                redisTemplate.opsForValue().set(cacheKey, "1", Duration.ofDays(30));
            } else {
                // 取消点赞时设置为"0"而非删除，防止缓存穿透
                redisTemplate.opsForValue().set(cacheKey, "0", Duration.ofDays(30));
            }
            
            log.info("评论{}成功, commentId: {}, userId: {}", isLike ? "点赞" : "取消点赞", commentId, userId);
            return isLike;
        } finally {
            // 释放锁
            try {
                redisTemplate.delete(lockKey);
            } catch (Exception e) {
                log.warn("释放锁失败: {}", lockKey, e);
            }
        }
    }

    /**
     * 获取评论的回复列表
     */
    @Override
    public IPage<CommentVO> getReplies(Long commentId, Integer page, Integer size, Long currentUserId) {
        log.info("获取评论回复列表, commentId: {}, page: {}, size: {}", commentId, page, size);
        
        // 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 查询回复列表
        Page<CommentVO> replyPage = new Page<>(page, size);
        IPage<CommentVO> result = commentMapper.selectRepliesPage(replyPage, commentId, currentUserId);
        
        // 处理回复
        for (CommentVO reply : result.getRecords()) {
            processComment(reply, currentUserId);
        }
        
        return result;
    }

    /**
     * 获取评论详情
     */
    @Override
    public CommentVO getCommentDetail(Long commentId, Long currentUserId) {
        log.info("获取评论详情, commentId: {}", commentId);
        
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 转换为VO（这里简化处理，实际应该关联查询用户信息）
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(comment.getReplyCount());
        vo.setIsHot(comment.getIsHot() == 1);
        vo.setIpLocation(comment.getIpLocation());
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(comment.getCreateTime());
        vo.setUserId(comment.getUserId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        
        // 判断是否已点赞
        if (currentUserId != null) {
            vo.setIsLiked(isLiked(commentId, currentUserId));
            vo.setIsAuthor(comment.getUserId().equals(currentUserId));
        }
        
        processComment(vo, currentUserId);
        return vo;
    }

    /**
     * 统计帖子评论数
     */
    @Override
    public int countByPostId(Long postId) {
        try {
            // 先从缓存获取
            String cacheKey = REDIS_KEY_COMMENT_COUNT + postId;
            String count = redisTemplate.opsForValue().get(cacheKey);
            if (count != null) {
                return Integer.parseInt(count);
            }
        } catch (Exception e) {
            log.warn("获取评论数缓存失败, postId: {}", postId, e);
        }
        
        // 从数据库查询
        int commentCount = commentMapper.countByPostId(postId);
        
        // 写入缓存
        try {
            String cacheKey = REDIS_KEY_COMMENT_COUNT + postId;
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(commentCount), Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("写入评论数缓存失败, postId: {}", postId, e);
        }
        
        return commentCount;
    }

    /**
     * 批量获取帖子的评论数（优化N+1查询）
     */
    @Override
    public Map<Long, Integer> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 使用批量查询替代循环查询，优化N+1问题
        List<Map<String, Object>> results = commentMapper.countByPostIds(postIds);
        
        // 将查询结果转换为Map
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long postId = ((Number) row.get("post_id")).longValue();
            Integer count = ((Number) row.get("count")).intValue();
            result.put(postId, count);
        }
        
        // 对于没有评论的帖子，补充0值
        for (Long postId : postIds) {
            if (!result.containsKey(postId)) {
                result.put(postId, 0);
            }
        }
        
        return result;
    }

    /**
     * 检查用户是否已点赞评论
     */
    @Override
    public boolean isLiked(Long commentId, Long userId) {
        // 先从缓存查询
        String cacheKey = REDIS_KEY_COMMENT_LIKE + commentId + ":" + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return "1".equals(cached);
        }
        
        // 从数据库查询
        CommentLike like = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        boolean isLiked = like != null;
        
        // 写入缓存
        if (isLiked) {
            redisTemplate.opsForValue().set(cacheKey, "1", Duration.ofDays(30));
        }
        
        return isLiked;
    }

    /**
     * 获取用户的评论列表
     */
    @Override
    public IPage<CommentVO> getCommentsByUserId(Long userId, Integer page, Integer size, Long currentUserId) {
        log.info("获取用户评论列表, userId: {}, page: {}, size: {}", userId, page, size);
        
        // 构建分页参数
        Page<CommentVO> pageParam = new Page<>(page, size);
        
        // 查询用户的评论列表
        IPage<CommentVO> commentPage = commentMapper.selectCommentsByUserId(pageParam, userId, currentUserId);
        
        // 处理每条评论
        for (CommentVO comment : commentPage.getRecords()) {
            processComment(comment, currentUserId);
        }
        
        return commentPage;
    }

    /**
     * 审核评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditComment(Long commentId, Integer status, String remark) {
        log.info("审核评论, commentId: {}, status: {}, remark: {}", commentId, status, remark);
        
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 2. 更新审核状态
        comment.setAuditStatus(status);
        // 如果审核不通过，设置评论状态为屏蔽
        if (status == 2) {
            comment.setStatus(2); // 2表示被系统屏蔽
        } else {
            comment.setStatus(1); // 审核通过，恢复正常状态
        }
        
        int result = commentMapper.updateById(comment);
        
        log.info("审核评论完成, commentId: {}, status: {}", commentId, status);
        return result > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 校验评论参数
     */
    private void validateComment(CommentCreateDTO createDTO) {
        if (createDTO.getPostId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子ID不能为空");
        }
        
        // 验证帖子是否存在
        try {
            Result<PostDTO> postResult = postApi.getPostById(createDTO.getPostId());
            if (postResult == null || !postResult.isSuccess() || postResult.getData() == null) {
                throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或已删除");
            }
            // 检查帖子状态是否正常
            PostDTO post = postResult.getData();
            if (post.getStatus() == null || post.getStatus() != 1) {
                throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或已被删除");
            }
        } catch (BusinessException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.error("验证帖子存在性失败, postId: {}", createDTO.getPostId(), e);
            // 服务调用失败时，为了用户体验，允许评论提交，但记录警告
            log.warn("帖子服务不可用，跳过帖子验证: {}", e.getMessage());
        }
        
        if (StrUtil.isBlank(createDTO.getContent())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "评论内容不能为空");
        }
        if (createDTO.getContent().length() > commentConfig.getMaxContentLength()) {
            throw new BusinessException(ResultCode.COMMENT_TOO_LONG);
        }
    }

    /**
     * 敏感词过滤
     */
    private String filterSensitiveWords(String content) {
        String result = content;
        for (String word : SENSITIVE_WORDS) {
            if (result.contains(word)) {
                result = result.replace(word, commentConfig.getSensitiveReplacement());
            }
        }
        
        // 过滤@用户（提取用户ID并记录）
        Pattern pattern = Pattern.compile("@([\\u4e00-\\u9fa5a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(result);
        while (matcher.find()) {
            String userName = matcher.group(1);
            log.debug("检测到@用户: {}", userName);
            // 实际项目中应该验证用户是否存在，并记录@关系
        }
        
        return result;
    }

    /**
     * 处理评论VO
     */
    private void processComment(CommentVO comment, Long currentUserId) {
        // 设置是否为作者
        if (currentUserId != null && comment.getUserId() != null) {
            comment.setIsAuthor(comment.getUserId().equals(currentUserId));
        }
        
        // 设置时间显示
        comment.setTimeAgo(formatTimeAgo(comment.getCreateTime()));
    }

    /**
     * 格式化时间显示
     */
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时前";
        } else if (seconds < 2592000) {
            return (seconds / 86400) + "天前";
        } else if (seconds < 31536000) {
            return (seconds / 2592000) + "个月前";
        } else {
            return (seconds / 31536000) + "年前";
        }
    }

    /**
     * 更新帖子评论数
     * 修复：同步更新数据库中的评论数，确保数据一致性
     */
    private void updatePostCommentCount(Long postId, int delta) {
        try {
            // 1. 更新缓存
            String cacheKey = REDIS_KEY_COMMENT_COUNT + postId;
            Boolean hasKey = redisTemplate.hasKey(cacheKey);
            if (Boolean.TRUE.equals(hasKey)) {
                redisTemplate.opsForValue().increment(cacheKey, delta);
            }
            // 如果缓存不存在，不创建，让下次查询时从数据库加载
            
            // 2. 【修复】同步更新帖子数据库中的评论数
            try {
                postApi.updatePostStats(postId, "commentCount", delta);
                log.debug("已同步更新帖子评论数到数据库, postId: {}, delta: {}", postId, delta);
            } catch (Exception e) {
                log.error("同步更新帖子评论数失败, postId: {}, delta: {}", postId, delta, e);
                // 数据库更新失败不影响主流程，但需要记录详细日志以便后续排查
            }
        } catch (Exception e) {
            log.warn("更新评论数缓存失败, postId: {}", postId, e);
        }
        
        log.debug("更新帖子评论数, postId: {}, delta: {}", postId, delta);
    }

    /**
     * 发送通知
     */
    private void sendNotification(Comment comment, String replyUserName) {
        try {
            // 如果是回复评论，通知被回复的用户
            if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(comment.getUserId())) {
                String title = "有人回复了您的评论";
                String content = String.format("用户回复了您的评论：%s", 
                        comment.getContent().length() > 50 ? 
                        comment.getContent().substring(0, 50) + "..." : 
                        comment.getContent());
                
                // 调用通知服务
                // notifyApi.sendNotice(comment.getReplyToUserId(), title, content);
                log.debug("发送回复通知给用户: {}", comment.getReplyToUserId());
            }
            
            // 如果是一级评论，通知帖子作者
            if (comment.getParentId() == 0) {
                // 查询帖子作者ID
                // Long postAuthorId = postApi.getPostAuthorId(comment.getPostId());
                // if (postAuthorId != null && !postAuthorId.equals(comment.getUserId())) {
                //     String title = "有人评论了您的帖子";
                //     String content = "您有新的评论，快来查看吧！";
                //     notifyApi.sendNotice(postAuthorId, title, content);
                // }
                log.debug("发送评论通知给帖子作者");
            }
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            log.error("发送通知失败", e);
        }
    }
}
