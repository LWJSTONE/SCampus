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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long followerId, Long followingId) {
        log.info("用户关注，关注者ID：{}，被关注者ID：{}", followerId, followingId);
        
        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能关注自己");
        }
        
        // 检查被关注的用户是否存在
        User followingUser = userMapper.selectById(followingId);
        if (followingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查是否已关注
        UserFollow existFollow = userFollowMapper.selectByFollowerAndFollowing(followerId, followingId);
        if (existFollow != null && existFollow.getStatus() == 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "已经关注了该用户");
        }
        
        if (existFollow != null) {
            // 之前关注过但取消了，更新状态为关注
            existFollow.setStatus(1);
            updateById(existFollow);
        } else {
            // 新建关注关系
            UserFollow userFollow = new UserFollow();
            userFollow.setFollowerId(followerId);
            userFollow.setFollowingId(followingId);
            userFollow.setStatus(1);
            save(userFollow);
        }
        
        // 更新用户的关注数和粉丝数
        userMapper.incrementFollowingCount(followerId);
        userMapper.incrementFollowerCount(followingId);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long followerId, Long followingId) {
        log.info("取消关注，关注者ID：{}，被关注者ID：{}", followerId, followingId);
        
        // 检查关注关系是否存在
        UserFollow existFollow = userFollowMapper.selectByFollowerAndFollowing(followerId, followingId);
        if (existFollow == null || existFollow.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "未关注该用户");
        }
        
        // 更新关注状态为取消
        existFollow.setStatus(0);
        updateById(existFollow);
        
        // 更新用户的关注数和粉丝数
        userMapper.decrementFollowingCount(followerId);
        userMapper.decrementFollowerCount(followingId);
        
        return true;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return userFollowMapper.isFollowing(followerId, followingId);
    }

    @Override
    public PageResult<UserFollowVO> getFollowers(Long userId, UserQueryDTO queryDTO) {
        log.info("获取粉丝列表，用户ID：{}", userId);
        
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
                        // 判断当前用户是否关注了粉丝
                        vo.setFollowed(isFollowing(userId, follow.getFollowerId()));
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
     *
     * @param user 用户实体
     * @return 用户关注VO
     */
    private UserFollowVO convertToVO(User user) {
        UserFollowVO vo = new UserFollowVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
