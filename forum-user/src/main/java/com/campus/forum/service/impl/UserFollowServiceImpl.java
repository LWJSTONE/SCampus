package com.campus.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.constant.ResultCode;
import com.campus.forum.dto.UserQueryDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserFollow;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.UserFollowMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.UserFollowService;
import com.campus.forum.vo.UserFollowVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 * 
 * 实现用户关注相关的业务逻辑：
 * - 关注/取消关注处理
 * - 粉丝/关注列表查询
 * - 关注数量统计
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String FOLLOW_LOCK_PREFIX = "follow:lock:";
    private static final long LOCK_WAIT_TIME = 10; // 锁过期时间（秒）

    // 使用ThreadLocal存储当前线程的锁值，用于安全释放锁
    private static final ThreadLocal<String> lockValueHolder = new ThreadLocal<>();

    /**
     * 获取分布式锁
     * 使用UUID作为锁值，确保只有锁持有者才能释放锁
     */
    private boolean tryLock(String key) {
        try {
            // 生成唯一的锁值
            String lockValue = UUID.randomUUID().toString();
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, lockValue, LOCK_WAIT_TIME, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(result)) {
                // 保存锁值到ThreadLocal，用于后续释放
                lockValueHolder.set(lockValue);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("获取分布式锁失败: {}", key, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     * 使用Lua脚本保证原子性，只有锁持有者才能释放锁
     */
    private void unlock(String key) {
        try {
            String lockValue = lockValueHolder.get();
            if (lockValue == null) {
                log.warn("释放分布式锁失败: 未找到锁值");
                return;
            }
            // 使用Lua脚本原子性释放锁：只有值匹配时才删除
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            redisTemplate.execute(
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(key),
                    lockValue
            );
            // 清理ThreadLocal
            lockValueHolder.remove();
        } catch (Exception e) {
            log.warn("释放分布式锁失败: {}", key, e);
            lockValueHolder.remove();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long followerId, Long followingId) {
        log.info("用户关注，关注者ID：{}，被关注者ID：{}", followerId, followingId);
        
        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能关注自己");
        }
        
        // 检查关注者状态，被封禁用户不能执行关注操作
        User followerUser = userMapper.selectById(followerId);
        if (followerUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "关注者不存在");
        }
        if (followerUser.getStatus() == null || followerUser.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "账户已被禁用，无法执行关注操作");
        }
        
        // 检查被关注的用户是否存在
        User followingUser = userMapper.selectById(followingId);
        if (followingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "被关注的用户不存在");
        }
        
        // 检查被关注用户的状态，不能关注被禁用的用户
        if (followingUser.getStatus() == null || followingUser.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该用户已被禁用，无法关注");
        }
        
        // 使用分布式锁防止并发问题
        String lockKey = FOLLOW_LOCK_PREFIX + followerId + ":" + followingId;
        if (!tryLock(lockKey)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "操作过于频繁，请稍后再试");
        }
        
        try {
            // 检查是否已关注（包括已取消的记录）
            UserFollow existFollow = userFollowMapper.selectByFollowerAndFollowing(followerId, followingId);
            
            // 标记是否需要更新计数器
            boolean needUpdateCount = false;
            
            if (existFollow != null) {
                if (existFollow.getStatus() == 1) {
                    // 已经关注了该用户，幂等操作：直接返回成功，不抛出异常，也不更新计数器
                    log.info("用户已关注过该用户，幂等返回成功：followerId={}, followingId={}", followerId, followingId);
                    return true;
                }
                // 之前关注过但取消了，更新状态为关注
                existFollow.setStatus(1);
                updateById(existFollow);
                needUpdateCount = true;
            } else {
                // 新建关注关系
                UserFollow userFollow = new UserFollow();
                userFollow.setFollowerId(followerId);
                userFollow.setFollowingId(followingId);
                userFollow.setStatus(1);
                try {
                    save(userFollow);
                    needUpdateCount = true;
                } catch (DuplicateKeyException e) {
                    // 并发场景：其他线程已插入，检查是否需要更新状态
                    log.info("并发关注检测到重复记录: followerId={}, followingId={}", followerId, followingId);
                    existFollow = userFollowMapper.selectByFollowerAndFollowing(followerId, followingId);
                    if (existFollow != null && existFollow.getStatus() != 1) {
                        existFollow.setStatus(1);
                        updateById(existFollow);
                        needUpdateCount = true;
                    }
                    // 无论是已关注还是刚被其他线程关注，都视为成功（幂等）
                }
            }
            
            // 只有在真正关注成功时才更新用户的关注数和粉丝数
            if (needUpdateCount) {
                userMapper.incrementFollowingCount(followerId);
                userMapper.incrementFollowerCount(followingId);
            }
            
            return true;
        } finally {
            unlock(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long followerId, Long followingId) {
        log.info("取消关注，关注者ID：{}，被关注者ID：{}", followerId, followingId);
        
        // 使用分布式锁防止并发问题
        String lockKey = FOLLOW_LOCK_PREFIX + followerId + ":" + followingId;
        if (!tryLock(lockKey)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "操作过于频繁，请稍后再试");
        }
        
        try {
            // 检查关注关系是否存在
            UserFollow existFollow = userFollowMapper.selectByFollowerAndFollowing(followerId, followingId);
            
            // 幂等处理：如果不存在关注关系或已经取消关注，直接返回成功
            if (existFollow == null || existFollow.getStatus() != 1) {
                log.info("用户未关注或已取消关注，幂等返回成功：followerId={}, followingId={}", followerId, followingId);
                return true;
            }
            
            // 更新关注状态为取消
            existFollow.setStatus(0);
            updateById(existFollow);
            
            // 更新用户的关注数和粉丝数
            userMapper.decrementFollowingCount(followerId);
            userMapper.decrementFollowerCount(followingId);
            
            return true;
        } finally {
            unlock(lockKey);
        }
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return userFollowMapper.isFollowing(followerId, followingId);
    }

    @Override
    public PageResult<UserFollowVO> getFollowers(Long userId, UserQueryDTO queryDTO, Long currentUserId) {
        log.info("获取粉丝列表，用户ID：{}，当前登录用户ID：{}", userId, currentUserId);
        
        // 构建分页对象
        Page<UserFollow> page = queryDTO.toPage();
        
        // 查询粉丝列表
        IPage<UserFollow> followPage = userFollowMapper.selectFollowerPage(page, userId);
        
        // 获取粉丝用户ID列表
        List<Long> followerIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());
        
        if (followerIds.isEmpty()) {
            return PageResult.build(followPage.getCurrent(), followPage.getSize(), followPage.getTotal(), new ArrayList<>());
        }
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(followerIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // 转换为VO
        List<UserFollowVO> voList = followPage.getRecords().stream()
                .map(follow -> {
                    User user = userMap.get(follow.getFollowerId());
                    if (user != null) {
                        UserFollowVO vo = convertToVO(user);
                        vo.setFollowTime(follow.getCreateTime());
                        // 判断当前登录用户是否关注了粉丝
                        // 使用currentUserId而非userId，修复了原来使用被查看用户ID判断关注关系的错误
                        if (currentUserId != null) {
                            vo.setFollowed(isFollowing(currentUserId, follow.getFollowerId()));
                        } else {
                            vo.setFollowed(false);
                        }
                        return vo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
        
        return PageResult.build(followPage.getCurrent(), followPage.getSize(), followPage.getTotal(), voList);
    }

    @Override
    public PageResult<UserFollowVO> getFollowing(Long userId, UserQueryDTO queryDTO) {
        log.info("获取关注列表，用户ID：{}", userId);
        
        // 构建分页对象
        Page<UserFollow> page = queryDTO.toPage();
        
        // 查询关注列表
        IPage<UserFollow> followPage = userFollowMapper.selectFollowingPage(page, userId);
        
        // 获取关注用户ID列表
        List<Long> followingIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());
        
        if (followingIds.isEmpty()) {
            return PageResult.build(followPage.getCurrent(), followPage.getSize(), followPage.getTotal(), new ArrayList<>());
        }
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(followingIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // 转换为VO
        List<UserFollowVO> voList = followPage.getRecords().stream()
                .map(follow -> {
                    User user = userMap.get(follow.getFollowingId());
                    if (user != null) {
                        UserFollowVO vo = convertToVO(user);
                        vo.setFollowTime(follow.getCreateTime());
                        // 已关注列表中的用户都是已关注的
                        vo.setFollowed(true);
                        return vo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
        
        return PageResult.build(followPage.getCurrent(), followPage.getSize(), followPage.getTotal(), voList);
    }

    @Override
    public int getFollowerCount(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }

    @Override
    public int getFollowingCount(Long userId) {
        return userFollowMapper.countFollowing(userId);
    }

    /**
     * 转换为VO
     * 手动设置需要的字段，避免使用BeanUtil.copyProperties盲目复制敏感信息
     *
     * @param user 用户实体
     * @return 用户关注VO
     */
    private UserFollowVO convertToVO(User user) {
        UserFollowVO vo = new UserFollowVO();
        // 手动设置允许暴露的非敏感字段
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setMajor(user.getMajor());
        vo.setFollowerCount(user.getFollowerCount());
        vo.setFollowingCount(user.getFollowingCount());
        return vo;
    }
}
