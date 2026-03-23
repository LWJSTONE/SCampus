package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Collect;
import com.campus.forum.mapper.CollectMapper;
import com.campus.forum.service.CollectService;
import com.campus.forum.vo.CollectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        // 查询是否已收藏
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getPostId, postId)
               .eq(Collect::getUserId, userId);
        
        Collect existCollect = collectMapper.selectOne(wrapper);
        
        if (existCollect != null) {
            // 已存在，执行取消收藏（物理删除）
            collectMapper.deleteById(existCollect.getId());
            
            // 更新缓存
            updateCollectCountCache(postId, -1);
            redisTemplate.opsForValue().set(getUserCollectKey(postId, userId), "0", CACHE_EXPIRE, TimeUnit.SECONDS);
            
            log.info("取消收藏: postId={}, userId={}", postId, userId);
            return false;
        } else {
            // 不存在，执行收藏
            Collect collect = new Collect();
            collect.setPostId(postId);
            collect.setUserId(userId);
            collect.setFolderId(folderId);
            
            try {
                collectMapper.insert(collect);
            } catch (DuplicateKeyException e) {
                // 并发场景：其他线程已插入，视为收藏成功
                log.info("并发收藏检测到重复记录: postId={}, userId={}", postId, userId);
            }
            
            // 更新缓存
            updateCollectCountCache(postId, 1);
            redisTemplate.opsForValue().set(getUserCollectKey(postId, userId), "1", CACHE_EXPIRE, TimeUnit.SECONDS);
            
            log.info("收藏成功: postId={}, userId={}", postId, userId);
            return true;
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
            return Integer.parseInt(cached);
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
}
