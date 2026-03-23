package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.api.post.PostApi;
import com.campus.forum.api.post.PostDTO;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.entity.Collect;
import com.campus.forum.entity.Result;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.CollectMapper;
import com.campus.forum.service.CollectService;
import com.campus.forum.vo.CollectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 收藏服务实现类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final CollectMapper collectMapper;
    private final StringRedisTemplate redisTemplate;
    private final PostApi postApi;

    private static final String COLLECT_COUNT_KEY = "collect:count:";
    private static final String COLLECT_USER_KEY = "collect:user:";
    private static final long CACHE_EXPIRE = 86400; // 24小时

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collect(Long postId, Long userId, Long folderId) {
        // 检查参数有效性
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("无效的帖子ID");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        
        // 验证帖子是否存在
        validatePostExists(postId);
        
        // 使用分布式锁防止并发问题
        String lockKey = "collect:lock:" + postId + ":" + userId;
        String lockValue = UUID.randomUUID().toString();
        try {
            // 尝试获取锁，5秒过期（增加过期时间以应对高并发）
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 5, java.util.concurrent.TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new RuntimeException("操作过于频繁，请稍后再试");
            }
            
            // 查询是否已收藏（包括已取消的记录）
            LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collect::getPostId, postId)
                   .eq(Collect::getUserId, userId);
            
            Collect existCollect = collectMapper.selectOne(wrapper);
            
            if (existCollect != null) {
                // 已存在记录，检查状态
                if (existCollect.getDeleteFlag() == null || existCollect.getDeleteFlag() == 0) {
                    // 当前是收藏状态，执行取消收藏（逻辑删除）
                    existCollect.setDeleteFlag(1);
                    collectMapper.updateById(existCollect);
                    
                    // 更新缓存
                    updateCollectCountCache(postId, -1);
                    redisTemplate.opsForValue().set(getUserCollectKey(postId, userId), "0", CACHE_EXPIRE, TimeUnit.SECONDS);
                    
                    log.info("取消收藏: postId={}, userId={}", postId, userId);
                    return false;
                } else {
                    // 当前是取消状态，恢复收藏
                    existCollect.setDeleteFlag(0);
                    existCollect.setFolderId(folderId); // 更新收藏夹
                    collectMapper.updateById(existCollect);
                    
                    // 更新缓存
                    updateCollectCountCache(postId, 1);
                    redisTemplate.opsForValue().set(getUserCollectKey(postId, userId), "1", CACHE_EXPIRE, TimeUnit.SECONDS);
                    
                    log.info("收藏成功: postId={}, userId={}", postId, userId);
                    return true;
                }
            } else {
                // 不存在记录，创建新的收藏记录
                Collect collect = new Collect();
                collect.setPostId(postId);
                collect.setUserId(userId);
                collect.setFolderId(folderId);
                collect.setDeleteFlag(0); // 使用逻辑删除标记，0表示正常
                
                try {
                    collectMapper.insert(collect);
                } catch (DuplicateKeyException e) {
                    // 并发场景：其他线程已插入，重新查询并更新状态
                    log.info("并发收藏检测到重复记录: postId={}, userId={}", postId, userId);
                    // 重新查询记录状态
                    Collect existingRecord = collectMapper.selectOne(wrapper);
                    if (existingRecord != null && (existingRecord.getDeleteFlag() == null || existingRecord.getDeleteFlag() == 1)) {
                        // 记录存在但已取消，恢复收藏
                        existingRecord.setDeleteFlag(0);
                        existingRecord.setFolderId(folderId);
                        collectMapper.updateById(existingRecord);
                    }
                }
                
                // 更新缓存
                updateCollectCountCache(postId, 1);
                redisTemplate.opsForValue().set(getUserCollectKey(postId, userId), "1", CACHE_EXPIRE, TimeUnit.SECONDS);
                
                log.info("收藏成功: postId={}, userId={}", postId, userId);
                return true;
            }
        } finally {
            // 安全释放锁：使用Lua脚本确保只有锁持有者才能释放锁
            try {
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), 
                        Collections.singletonList(lockKey), lockValue);
            } catch (Exception e) {
                log.warn("释放收藏锁失败: {}", lockKey, e);
            }
        }
    }

    @Override
    public boolean isCollected(Long postId, Long userId) {
        // 先从缓存获取
        String key = getUserCollectKey(postId, userId);
        String cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) {
            return "1".equals(cached);
        }
        
        // 从数据库查询
        int count = collectMapper.checkCollected(postId, userId);
        boolean collected = count > 0;
        
        // 写入缓存
        redisTemplate.opsForValue().set(key, collected ? "1" : "0", CACHE_EXPIRE, TimeUnit.SECONDS);
        
        return collected;
    }

    @Override
    public int getCollectCount(Long postId) {
        // 先从缓存获取
        String key = getCollectCountKey(postId);
        String cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) {
            try {
                return Integer.parseInt(cached);
            } catch (NumberFormatException e) {
                // 缓存数据格式异常，删除无效缓存并从数据库重新加载
                log.warn("收藏数缓存解析失败, key={}, cachedValue={}", key, cached, e);
                redisTemplate.delete(key);
            }
        }
        
        // 从数据库查询
        int count = collectMapper.countByPostId(postId);
        
        // 写入缓存
        redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_EXPIRE, TimeUnit.SECONDS);
        
        return count;
    }

    @Override
    public IPage<CollectVO> getCollectList(Long userId, Integer page, Integer size) {
        Page<CollectVO> pageParam = new Page<>(page, size);
        return collectMapper.selectCollectPage(pageParam, userId);
    }

    /**
     * 更新收藏数缓存
     */
    private void updateCollectCountCache(Long postId, int delta) {
        String key = getCollectCountKey(postId);
        try {
            // 检查key是否存在
            Boolean hasKey = redisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                redisTemplate.opsForValue().increment(key, delta);
            } else {
                // key不存在时，从数据库加载真实值
                int count = collectMapper.countByPostId(postId);
                redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_EXPIRE, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("更新收藏数缓存失败, postId={}", postId, e);
        }
    }

    /**
     * 获取收藏数缓存key
     */
    private String getCollectCountKey(Long postId) {
        return COLLECT_COUNT_KEY + postId;
    }

    /**
     * 获取用户收藏状态缓存key
     */
    private String getUserCollectKey(Long postId, Long userId) {
        return COLLECT_USER_KEY + postId + ":" + userId;
    }

    /**
     * 验证帖子是否存在
     * @param postId 帖子ID
     */
    private void validatePostExists(Long postId) {
        try {
            Result<PostDTO> postResult = postApi.getPostById(postId);
            if (postResult == null || !postResult.isSuccess() || postResult.getData() == null) {
                throw new BusinessException(ResultCode.POST_NOT_FOUND, "帖子不存在或已删除");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证帖子存在性失败, postId={}", postId, e);
            // 如果远程服务调用失败，记录日志但不阻止操作（服务降级处理）
            // 生产环境可以根据实际需求决定是否抛出异常
        }
    }
}
