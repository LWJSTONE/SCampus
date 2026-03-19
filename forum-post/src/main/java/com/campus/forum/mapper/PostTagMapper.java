package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子标签关联Mapper接口
 * 提供帖子标签关联数据的数据库操作
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    /**
     * 根据帖子ID查询标签列表
     *
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<PostTag> selectByPostId(@Param("postId") Long postId);

    /**
     * 根据标签ID查询帖子ID列表
     *
     * @param tagId 标签ID
     * @return 帖子ID列表
     */
    List<Long> selectPostIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 批量插入帖子标签关联
     *
     * @param postId 帖子ID
     * @param tags 标签列表
     * @return 影响行数
     */
    int batchInsert(@Param("postId") Long postId, @Param("tags") List<PostTag> tags);

    /**
     * 根据帖子ID删除标签关联
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 根据标签ID统计帖子数量
     *
     * @param tagId 标签ID
     * @return 帖子数量
     */
    int countByTagId(@Param("tagId") Long tagId);

    /**
     * 查询热门标签
     *
     * @param limit 数量限制
     * @return 热门标签列表
     */
    List<PostTag> selectHotTags(@Param("limit") Integer limit);

    /**
     * 更新帖子标签
     *
     * @param postId 帖子ID
     * @param tags 新的标签列表
     * @return 影响行数
     */
    int updatePostTags(@Param("postId") Long postId, @Param("tags") List<PostTag> tags);
}
