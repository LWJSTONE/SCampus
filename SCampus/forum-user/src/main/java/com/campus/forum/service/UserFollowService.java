package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.dto.UserQueryDTO;
import com.campus.forum.entity.PageResult;
import com.campus.forum.entity.UserFollow;
import com.campus.forum.vo.UserFollowVO;

/**
 * 用户关注服务接口
 * 
 * 提供用户关注相关的业务功能：
 * - 关注/取消关注
 * - 获取粉丝列表
 * - 获取关注列表
 * - 判断关注关系
 *
 * @author campus
 * @since 2024-01-01
 */
public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 是否成功
     */
    boolean follow(Long followerId, Long followingId);

    /**
     * 取消关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 是否成功
     */
    boolean unfollow(Long followerId, Long followingId);

    /**
     * 判断是否已关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取粉丝列表
     *
     * @param userId   用户ID
     * @param queryDTO 查询条件
     * @return 粉丝列表
     */
    PageResult<UserFollowVO> getFollowers(Long userId, UserQueryDTO queryDTO);

    /**
     * 获取关注列表
     *
     * @param userId   用户ID
     * @param queryDTO 查询条件
     * @return 关注列表
     */
    PageResult<UserFollowVO> getFollowing(Long userId, UserQueryDTO queryDTO);

    /**
     * 获取粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    int getFollowerCount(Long userId);

    /**
     * 获取关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    int getFollowingCount(Long userId);
}
