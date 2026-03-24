package com.campus.forum.service.impl;

import com.campus.forum.entity.Mention;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.MentionMapper;
import com.campus.forum.service.MentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @提及服务实现类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private final MentionMapper mentionMapper;
    private final StringRedisTemplate redisTemplate;
    
    private static final String MENTION_LOCK_PREFIX = "mention:lock:";
    private static final long LOCK_EXPIRE_TIME = 5; // 锁过期时间（秒）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMention(Integer targetType, Long targetId, Long toUserId, Long fromUserId, String content) {
        // 参数验证
        if (targetType == null || (targetType != 1 && targetType != 2)) {
            throw new BusinessException("目标类型无效，必须为1(帖子)或2(评论)");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID无效");
        }
        if (toUserId == null || toUserId <= 0) {
            throw new BusinessException("被提及用户ID无效");
        }
        if (fromUserId == null || fromUserId <= 0) {
            throw new BusinessException("发起用户ID无效");
        }
        
        // 防止自己@自己
        if (toUserId.equals(fromUserId)) {
            log.debug("用户不能@自己，跳过创建提及: userId={}", fromUserId);
            return null;
        }
        
        // 【修复】使用分布式锁防止并发重复提及
        // 锁的粒度：发起用户+被提及用户+目标类型+目标ID
        String lockKey = MENTION_LOCK_PREFIX + fromUserId + ":" + toUserId + ":" + targetType + ":" + targetId;
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                log.debug("获取提及锁失败，可能存在并发请求: lockKey={}", lockKey);
                // 并发请求，返回null跳过
                return null;
            }
            
            // 防止重复提及（同一用户在同一目标中被重复@）
            int existingCount = mentionMapper.countExistingMention(fromUserId, toUserId, targetType, targetId);
            if (existingCount > 0) {
                log.debug("已存在相同的提及记录，跳过创建: fromUserId={}, toUserId={}, targetType={}, targetId={}", 
                        fromUserId, toUserId, targetType, targetId);
                return null;
            }
            
            Mention mention = new Mention();
            mention.setTargetType(targetType);
            mention.setTargetId(targetId);
            mention.setToUserId(toUserId);
            mention.setFromUserId(fromUserId);
            mention.setIsRead(0);
            
            // 【修复】处理数据库唯一约束并发插入的情况
            try {
                mentionMapper.insert(mention);
            } catch (DuplicateKeyException e) {
                log.info("并发插入检测到重复提及记录: fromUserId={}, toUserId={}, targetType={}, targetId={}", 
                        fromUserId, toUserId, targetType, targetId);
                return null;
            }
            
            log.info("创建@提及: targetType={}, targetId={}, toUserId={}, fromUserId={}", 
                    targetType, targetId, toUserId, fromUserId);
            
            return mention.getId();
        } finally {
            // 【修复】安全释放锁：使用Lua脚本确保只有锁持有者才能释放锁
            try {
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), 
                        Collections.singletonList(lockKey), lockValue);
            } catch (Exception e) {
                log.warn("释放提及锁失败: {}", lockKey, e);
            }
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return mentionMapper.countUnread(userId);
    }

    @Override
    public List<Mention> getMentionList(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return mentionMapper.selectByUserId(userId);
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        // 通过在SQL中添加userId条件实现归属权验证
        // 如果userId不匹配，更新将影响0行，返回false
        return mentionMapper.markAsRead(id, userId) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        return mentionMapper.markAllAsRead(userId) > 0;
    }
}
