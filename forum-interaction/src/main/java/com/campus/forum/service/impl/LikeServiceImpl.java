package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.api.comment.CommentApi;
import com.campus.forum.api.comment.CommentDTO;
import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.post.PostDTO;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.entity.Like;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.LikeMapper;
import com.campus.forum.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 点赞服务实现类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final StringRedisTemplate redisTemplate;
    private final PostApi postApi;
    private final CommentApi commentApi;

    private static final String LIKE_COUNT_KEY = "like:count:";
    private static final String LIKE_USER_KEY = "like:user:";
    private static final long CACHE_EXPIRE = 86400; // 24小时

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean like(Integer targetType, Long targetId, Long userId) {
        // 检查参数有效性
        if (targetType == null || (targetType != 1 && targetType != 2)) {
            throw new IllegalArgumentException("无效的目标类型");
        }
        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("无效的目标ID");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        
        // 目标存在性验证：targetType=1为帖子，targetType=2为评论
        validateTargetExists(targetType, targetId);
        
        // 使用分布式锁防止并发问题
        String lockKey = "like:lock:" + targetType + ":" + targetId + ":" + userId;
        try {
            // 尝试获取锁，3秒过期
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, java.util.concurrent.TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new RuntimeException("操作过于频繁，请稍后再试");
            }
            
            // 查询是否已点赞（包括已取消的记录）
            LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Like::getTargetType, targetType)
                   .eq(Like::getTargetId, targetId)
                   .eq(Like::getUserId, userId);
            
            Like existLike = likeMapper.selectOne(wrapper);
            
            if (existLike != null) {
                // 已存在记录，检查状态
                if (existLike.getDeleteFlag() == null || existLike.getDeleteFlag() == 0) {
                    // 当前是点赞状态，执行取消点赞（逻辑删除）
                    existLike.setDeleteFlag(1);
                    likeMapper.updateById(existLike);
                    
                    // 更新缓存
                    updateLikeCountCache(targetType, targetId, -1);
                    redisTemplate.opsForValue().set(getUserLikeKey(targetType, targetId, userId), "0", CACHE_EXPIRE, TimeUnit.SECONDS);
                    
                    log.info("取消点赞: targetType={}, targetId={}, userId={}", targetType, targetId, userId);
                    return false;
                } else {
                    // 当前是取消状态，恢复点赞
                    existLike.setDeleteFlag(0);
                    likeMapper.updateById(existLike);
                    
                    // 更新缓存
                    updateLikeCountCache(targetType, targetId, 1);
                    redisTemplate.opsForValue().set(getUserLikeKey(targetType, targetId, userId), "1", CACHE_EXPIRE, TimeUnit.SECONDS);
                    
                    log.info("点赞成功: targetType={}, targetId={}, userId={}", targetType, targetId, userId);
                    return true;
                }
            } else {
                // 不存在记录，创建新的点赞记录
                Like like = new Like();
                like.setTargetType(targetType);
                like.setTargetId(targetId);
                like.setUserId(userId);
                like.setDeleteFlag(0); // 使用逻辑删除标记，0表示正常
                
                try {
                    likeMapper.insert(like);
                } catch (DuplicateKeyException e) {
                    // 并发场景：其他线程已插入，视为点赞成功
                    log.info("并发点赞检测到重复记录: targetType={}, targetId={}, userId={}", targetType, targetId, userId);
                }
                
                // 更新缓存
                updateLikeCountCache(targetType, targetId, 1);
                redisTemplate.opsForValue().set(getUserLikeKey(targetType, targetId, userId), "1", CACHE_EXPIRE, TimeUnit.SECONDS);
                
                log.info("点赞成功: targetType={}, targetId={}, userId={}", targetType, targetId, userId);
                return true;
            }
        } finally {
            // 释放锁
            try {
                redisTemplate.delete(lockKey);
            } catch (Exception e) {
                log.warn("释放点赞锁失败: {}", lockKey, e);
            }
        }
    }

    @Override
    public boolean isLiked(Integer targetType, Long targetId, Long userId) {
        // 先从缓存获取
        String key = getUserLikeKey(targetType, targetId, userId);
        String cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) {
            return "1".equals(cached);
        }
        
        // 从数据库查询
        int count = likeMapper.checkLiked(targetType, targetId, userId);
        boolean liked = count > 0;
        
        // 写入缓存
        redisTemplate.opsForValue().set(key, liked ? "1" : "0", CACHE_EXPIRE, TimeUnit.SECONDS);
        
        return liked;
    }

    @Override
    public int getLikeCount(Integer targetType, Long targetId) {
        // 先从缓存获取
        String key = getLikeCountKey(targetType, targetId);
        String cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) {
            return Integer.parseInt(cached);
        }
        
        // 从数据库查询
        int count = likeMapper.countByTarget(targetType, targetId);
        
        // 写入缓存
        redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_EXPIRE, TimeUnit.SECONDS);
        
        return count;
    }

    /**
     * 更新点赞数缓存
     */
    private void updateLikeCountCache(Integer targetType, Long targetId, int delta) {
        String key = getLikeCountKey(targetType, targetId);
        try {
            // 检查key是否存在
            Boolean hasKey = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                redisTemplate.opsForValue().increment(key, delta);
            } else {
                // key不存在时，从数据库加载真实值
                int count = likeMapper.countByTarget(targetType, targetId);
                redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_EXPIRE, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("更新点赞数缓存失败, targetType={}, targetId={}", targetType, targetId, e);
        }
    }

    /**
     * 获取点赞数缓存key
     */
    private String getLikeCountKey(Integer targetType, Long targetId) {
        return LIKE_COUNT_KEY + targetType + ":" + targetId;
    }

    /**
     * 获取用户点赞状态缓存key
     */
    private String getUserLikeKey(Integer targetType, Long targetId, Long userId) {
        return LIKE_USER_KEY + targetType + ":" + targetId + ":" + userId;
    }

    /**
     * 验证点赞目标是否存在
     * @param targetType 目标类型：1-帖子，2-评论
     * @param targetId 目标ID
     */
    private void validateTargetExists(Integer targetType, Long targetId) {
        try {
            if (targetType == 1) {
                // 验证帖子是否存在
                Result<PostDTO> postResult = postApi.getPostById(targetId);
                if (postResult == null || !postResult.isSuccess() || postResult.getData() == null) {
                    throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或已删除");
                }
            } else if (targetType == 2) {
                // 验证评论是否存在
                Result<CommentDTO> commentResult = commentApi.getCommentById(targetId);
                if (commentResult == null || !commentResult.isSuccess() || commentResult.getData() == null) {
                    throw new BusinessException(ResultCode.COMMENT_NOT_FOUND, "评论不存在或已删除");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证点赞目标存在性失败, targetType={}, targetId={}", targetType, targetId, e);
            // 如果远程服务调用失败，记录日志但不阻止操作（服务降级处理）
            // 生产环境可以根据实际需求决定是否抛出异常
        }
    }
}
