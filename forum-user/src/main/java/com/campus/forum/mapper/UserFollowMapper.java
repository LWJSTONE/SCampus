package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注Mapper接口
 * 
 * 提供用户关注数据的持久化操作，包括：
 * - 基础CRUD操作（继承自BaseMapper）
 * - 关注/取消关注
 * - 查询粉丝列表
 * - 查询关注列表
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 查询关注关系
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 关注关系实体
     */
    @Select("SELECT * FROM sys_user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId} AND delete_flag = 0")
    UserFollow selectByFollowerAndFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 查询关注状态（包含已取消的记录）
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 关注关系实体
     */
    @Select("SELECT * FROM sys_user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    UserFollow selectFollowStatus(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 检查是否已关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    @Select("SELECT COUNT(1) > 0 FROM sys_user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId} AND status = 1 AND delete_flag = 0")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 分页查询粉丝列表
     *
     * @param page        分页对象
     * @param followingId 被关注者ID
     * @return 粉丝列表
     */
    @Select("SELECT * FROM sys_user_follow WHERE following_id = #{followingId} AND status = 1 AND delete_flag = 0 ORDER BY create_time DESC")
    IPage<UserFollow> selectFollowerPage(Page<UserFollow> page, @Param("followingId") Long followingId);

    /**
     * 分页查询关注列表
     *
     * @param page       分页对象
     * @param followerId 关注者ID
     * @return 关注列表
     */
    @Select("SELECT * FROM sys_user_follow WHERE follower_id = #{followerId} AND status = 1 AND delete_flag = 0 ORDER BY create_time DESC")
    IPage<UserFollow> selectFollowingPage(Page<UserFollow> page, @Param("followerId") Long followerId);

    /**
     * 统计粉丝数量
     *
     * @param followingId 被关注者ID
     * @return 粉丝数量
     */
    @Select("SELECT COUNT(1) FROM sys_user_follow WHERE following_id = #{followingId} AND status = 1 AND delete_flag = 0")
    int countFollowers(@Param("followingId") Long followingId);

    /**
     * 统计关注数量
     *
     * @param followerId 关注者ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(1) FROM sys_user_follow WHERE follower_id = #{followerId} AND status = 1 AND delete_flag = 0")
    int countFollowing(@Param("followerId") Long followerId);
}
