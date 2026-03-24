package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Moderator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 版主Mapper接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface ModeratorMapper extends BaseMapper<Moderator> {

    /**
     * 根据版块ID查询版主列表
     *
     * @param forumId 版块ID
     * @return 版主列表
     */
    @Select("SELECT * FROM forum_moderator WHERE forum_id = #{forumId} AND delete_flag = 0 ORDER BY is_primary DESC, id ASC")
    List<Moderator> selectByForumId(@Param("forumId") Long forumId);

    /**
     * 根据用户ID查询管理的版块
     *
     * @param userId 用户ID
     * @return 版主记录列表
     */
    @Select("SELECT * FROM forum_moderator WHERE user_id = #{userId} AND delete_flag = 0")
    List<Moderator> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否为版块版主
     *
     * @param forumId 版块ID
     * @param userId  用户ID
     * @return 版主记录
     */
    @Select("SELECT * FROM forum_moderator WHERE forum_id = #{forumId} AND user_id = #{userId} AND delete_flag = 0")
    Moderator selectByForumIdAndUserId(@Param("forumId") Long forumId, @Param("userId") Long userId);

    /**
     * 查询版块的主版主
     *
     * @param forumId 版块ID
     * @return 主版主
     */
    @Select("SELECT * FROM forum_moderator WHERE forum_id = #{forumId} AND is_primary = 1 AND delete_flag = 0 LIMIT 1")
    Moderator selectPrimaryModerator(@Param("forumId") Long forumId);

    /**
     * 统计版块的版主数量
     *
     * @param forumId 版块ID
     * @return 版主数量
     */
    @Select("SELECT COUNT(*) FROM forum_moderator WHERE forum_id = #{forumId} AND delete_flag = 0")
    int countByForumId(@Param("forumId") Long forumId);

    /**
     * 删除版块的所有版主
     *
     * @param forumId 版块ID
     * @return 影响行数
     */
    @Update("UPDATE forum_moderator SET delete_flag = 1, update_time = NOW() WHERE forum_id = #{forumId}")
    int deleteByForumId(@Param("forumId") Long forumId);

    /**
     * 更新版主状态
     *
     * @param id     记录ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE forum_moderator SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 设置主版主
     *
     * @param forumId 版块ID
     * @param userId  用户ID
     * @return 影响行数
     */
    @Update("UPDATE forum_moderator SET is_primary = 1, update_time = NOW() WHERE forum_id = #{forumId} AND user_id = #{userId} AND delete_flag = 0")
    int setPrimaryModerator(@Param("forumId") Long forumId, @Param("userId") Long userId);

    /**
     * 取消主版主
     *
     * @param forumId 版块ID
     * @return 影响行数
     */
    @Update("UPDATE forum_moderator SET is_primary = 0, update_time = NOW() WHERE forum_id = #{forumId}")
    int clearPrimaryModerator(@Param("forumId") Long forumId);

    /**
     * 批量查询多个版块的版主列表
     * 【性能优化】用于解决N+1查询问题，一次查询获取所有版块的版主信息
     *
     * @param forumIds 版块ID列表
     * @return 所有版块的版主列表
     */
    @Select("<script>" +
            "SELECT * FROM forum_moderator WHERE forum_id IN " +
            "<foreach collection='forumIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND delete_flag = 0 ORDER BY is_primary DESC, id ASC" +
            "</script>")
    List<Moderator> selectByForumIds(@Param("forumIds") List<Long> forumIds);
}
