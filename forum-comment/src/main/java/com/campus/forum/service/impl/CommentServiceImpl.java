package com.campus.forum.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.api.notify.NotifyApi;
import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.user.UserApi;
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

    // 远程服务调用（需要时注入）
    // private final NotifyApi notifyApi;
    // private final PostApi postApi;
    // private final UserApi userApi;

    // Redis Key前缀
    private static final String REDIS_KEY_COMMENT_COUNT = "comment:count:";
    private static final String REDIS_KEY_COMMENT_LIKE = "comment:like:";
    private static final String REDIS_KEY_USER_COMMENTS = "comment:user:";
    
    // 敏感词列表（实际项目中应从数据库或配置中心获取）
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "敏感词1", "敏感词2", "违禁词", "广告"
    ));

    /**
     * 获取帖子的评论列表
     */
    @Override
    public Page<CommentVO> getCommentsByPostId(Long postId, CommentQueryDTO queryDTO, Long currentUserId) {
        log.info("获取帖子评论列表, postId: {}, currentUserId: {}", postId, currentUserId);
        
        // 构建分页参数
        Page<CommentVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 查询一级评论
        Page<CommentVO> commentPage = commentMapper.selectCommentsByPostId(page, postId, currentUserId);
        
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
        comment.setStatus(0);  // 正常状态
        comment.setIsHot(0);   // 非热门
        comment.setAuditStatus(1);  // 审核通过（实际项目可能需要审核流程）
        comment.setIpAddress(ipAddress);
        
        // 解析IP归属地
        String ipLocation = IpUtils.getIpLocation(ipAddress);
        comment.setIpLocation(ipLocation);
        
        // 4. 处理父评论和回复用户
        Long parentId = createDTO.getParentId();
        if (parentId != null && parentId > 0) {
            // 验证父评论是否存在
            Comment parentComment = commentMapper.selectById(parentId);
            if (parentComment == null || parentComment.getDeleteFlag() == 1) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
            comment.setParentId(parentId);
            comment.setReplyToUserId(createDTO.getReplyToUserId() != null ? 
                    createDTO.getReplyToUserId() : parentComment.getUserId());
        } else {
            comment.setParentId(0L);
        }
        
        // 5. 保存评论
        commentMapper.insert(comment);
        
        // 6. 更新父评论的回复数
        if (parentId != null && parentId > 0) {
            commentMapper.incrementReplyCount(parentId);
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
        
        // 2. 权限校验（只能删除自己的评论）
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.COMMENT_NO_PERMISSION);
        }
        
        // 3. 逻辑删除评论
        comment.setDeleteFlag(1);
        comment.setStatus(1);  // 设置为已删除状态
        commentMapper.updateById(comment);
        
        // 4. 更新父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
        
        // 5. 更新帖子评论数
        updatePostCommentCount(comment.getPostId(), -1);
        
        log.info("评论删除成功, commentId: {}", commentId);
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
        
        // 2. 查询是否已点赞
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
        
        // 3. 更新热门状态
        Comment updatedComment = commentMapper.selectById(commentId);
        if (updatedComment.getLikeCount() >= commentConfig.getHotCommentThreshold()) {
            commentMapper.updateHotStatus(commentId, 1);
        } else {
            commentMapper.updateHotStatus(commentId, 0);
        }
        
        // 4. 更新缓存
        String cacheKey = REDIS_KEY_COMMENT_LIKE + commentId + ":" + userId;
        if (isLike) {
            redisTemplate.opsForValue().set(cacheKey, "1", Duration.ofDays(30));
        } else {
            redisTemplate.delete(cacheKey);
        }
        
        log.info("评论{}成功, commentId: {}, userId: {}", isLike ? "点赞" : "取消点赞", commentId, userId);
        return isLike;
    }

    /**
     * 获取评论的回复列表
     */
    @Override
    public Page<CommentVO> getReplies(Long commentId, Integer page, Integer size, Long currentUserId) {
        log.info("获取评论回复列表, commentId: {}, page: {}, size: {}", commentId, page, size);
        
        // 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        
        // 查询回复列表
        Page<CommentVO> replyPage = new Page<>(page, size);
        Page<CommentVO> result = commentMapper.selectRepliesPage(replyPage, commentId, currentUserId);
        
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
        // 先从缓存获取
        String cacheKey = REDIS_KEY_COMMENT_COUNT + postId;
        String count = redisTemplate.opsForValue().get(cacheKey);
        if (count != null) {
            return Integer.parseInt(count);
        }
        
        // 从数据库查询
        int commentCount = commentMapper.countByPostId(postId);
        
        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(commentCount), Duration.ofHours(1));
        
        return commentCount;
    }

    /**
     * 批量获取帖子的评论数
     */
    @Override
    public Map<Long, Integer> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, Integer> result = new HashMap<>();
        for (Long postId : postIds) {
            result.put(postId, countByPostId(postId));
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

    // ==================== 私有方法 ====================

    /**
     * 校验评论参数
     */
    private void validateComment(CommentCreateDTO createDTO) {
        if (createDTO.getPostId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "帖子ID不能为空");
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
     */
    private void updatePostCommentCount(Long postId, int delta) {
        // 更新缓存
        String cacheKey = REDIS_KEY_COMMENT_COUNT + postId;
        redisTemplate.opsForValue().increment(cacheKey, delta);
        
        // 实际项目中应该调用帖子服务更新数据库
        // postApi.updateCommentCount(postId, delta);
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
