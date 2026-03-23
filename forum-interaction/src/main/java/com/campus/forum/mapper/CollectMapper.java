package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Collect;
import com.campus.forum.vo.CollectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 收藏Mapper接口
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface CollectMapper extends BaseMapper<Collect> {

    /**
     * 统计帖子的收藏数
     * 注意：需要过滤已取消的收藏记录（delete_flag = 0）
     */
    @Select("SELECT COUNT(*) FROM forum_collect WHERE post_id = #{postId} AND delete_flag = 0")
    int countByPostId(@Param("postId") Long postId);

    /**
     * 检查是否已收藏
     * 注意：需要过滤已取消的收藏记录（delete_flag = 0）
     */
    @Select("SELECT COUNT(*) FROM forum_collect WHERE post_id = #{postId} AND user_id = #{userId} AND delete_flag = 0")
    int checkCollected(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 分页查询用户收藏列表
     * 注意：需要过滤已取消的收藏记录（delete_flag = 0）
     */
    @Select("SELECT c.id, c.post_id, c.create_time, " +
            "p.title as post_title, p.summary as post_summary, p.cover as post_cover, " +
            "u.id as author_id, u.nickname as author_name, u.avatar as author_avatar, " +
            "cat.id as category_id, cat.name as category_name " +
            "FROM forum_collect c " +
            "LEFT JOIN forum_post p ON c.post_id = p.id " +
            "LEFT JOIN sys_user u ON p.user_id = u.id " +
            "LEFT JOIN forum_category cat ON p.category_id = cat.id " +
            "WHERE c.user_id = #{userId} AND c.delete_flag = 0 " +
            "ORDER BY c.create_time DESC")
    IPage<CollectVO> selectCollectPage(Page<CollectVO> page, @Param("userId") Long userId);
}
