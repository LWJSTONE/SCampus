package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Forum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 版块Mapper接口
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface ForumMapper extends BaseMapper<Forum> {

    /**
     * 根据分类ID查询版块列表
     *
     * @param categoryId 分类ID
     * @return 版块列表
     */
    @Select("SELECT * FROM forum_forum WHERE category_id = #{categoryId} AND delete_flag = 0 ORDER BY sort ASC, id ASC")
    List<Forum> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询所有启用的版块
     *
     * @return 版块列表
     */
    @Select("SELECT * FROM forum_forum WHERE status = 1 AND delete_flag = 0 ORDER BY sort ASC, id ASC")
    List<Forum> selectAllActive();

    /**
     * 更新版块状态
     *
     * @param id     版块ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE forum_forum SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 增加帖子数量
     *
     * @param id 版块ID
     * @return 影响行数
     */
    @Update("UPDATE forum_forum SET post_count = post_count + 1, last_post_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int incrementPostCount(@Param("id") Long id);

    /**
     * 减少帖子数量
     *
     * @param id 版块ID
     * @return 影响行数
     */
    @Update("UPDATE forum_forum SET post_count = GREATEST(post_count - 1, 0), update_time = NOW() WHERE id = #{id}")
    int decrementPostCount(@Param("id") Long id);

    /**
     * 更新最后发帖信息
     *
     * @param id           版块ID
     * @param userId       用户ID
     * @param postTitle    帖子标题
     * @return 影响行数
     */
    @Update("UPDATE forum_forum SET last_post_time = NOW(), last_post_user_id = #{userId}, last_post_title = #{postTitle}, update_time = NOW() WHERE id = #{id}")
    int updateLastPost(@Param("id") Long id, @Param("userId") Long userId, @Param("postTitle") String postTitle);

    /**
     * 重置今日帖子数
     *
     * @return 影响行数
     */
    @Update("UPDATE forum_forum SET today_post_count = 0, update_time = NOW() WHERE delete_flag = 0")
    int resetTodayPostCount();

    /**
     * 检查版块名称是否存在
     *
     * @param name       版块名称
     * @param excludeId  排除的ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM forum_forum WHERE name = #{name} AND delete_flag = 0 AND id != #{excludeId}")
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);
}
