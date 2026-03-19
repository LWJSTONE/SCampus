package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Post;
import com.campus.forum.dto.PostQueryDTO;
import com.campus.forum.vo.PostListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子Mapper接口
 * 提供帖子数据的数据库操作
 *
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子列表
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 帖子列表
     */
    IPage<PostListVO> selectPostPage(Page<PostListVO> page, @Param("query") PostQueryDTO query);

    /**
     * 查询热门帖子列表
     *
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    List<PostListVO> selectHotPosts(@Param("limit") Integer limit);

    /**
     * 搜索帖子
     *
     * @param page 分页参数
     * @param keyword 关键词
     * @param query 查询条件
     * @return 搜索结果
     */
    IPage<PostListVO> searchPosts(Page<PostListVO> page, @Param("keyword") String keyword, @Param("query") PostQueryDTO query);

    /**
     * 根据ID查询帖子详情（包含用户和板块信息）
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    Post selectPostDetailById(@Param("id") Long id);

    /**
     * 增加浏览量
     *
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞数
     *
     * @param id 帖子ID
     * @param count 增加数量（可为负数表示减少）
     * @return 影响行数
     */
    int incrementLikeCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 增加评论数
     *
     * @param id 帖子ID
     * @param count 增加数量（可为负数表示减少）
     * @return 影响行数
     */
    int incrementCommentCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 增加收藏数
     *
     * @param id 帖子ID
     * @param count 增加数量（可为负数表示减少）
     * @return 影响行数
     */
    int incrementCollectCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 更新最后回复信息
     *
     * @param id 帖子ID
     * @param userId 回复用户ID
     * @return 影响行数
     */
    int updateLastReply(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 置顶帖子
     *
     * @param id 帖子ID
     * @param isTop 是否置顶
     * @return 影响行数
     */
    int updateTopStatus(@Param("id") Long id, @Param("isTop") Integer isTop);

    /**
     * 加精帖子
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 影响行数
     */
    int updateEssenceStatus(@Param("id") Long id, @Param("isEssence") Integer isEssence);

    /**
     * 移动帖子到其他板块
     *
     * @param id 帖子ID
     * @param forumId 目标板块ID
     * @return 影响行数
     */
    int movePost(@Param("id") Long id, @Param("forumId") Long forumId);

    /**
     * 关闭帖子
     *
     * @param id 帖子ID
     * @param status 状态
     * @return 影响行数
     */
    int closePost(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 批量更新帖子状态
     *
     * @param ids 帖子ID列表
     * @param status 状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 统计用户帖子数量
     *
     * @param userId 用户ID
     * @return 帖子数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计板块帖子数量
     *
     * @param forumId 板块ID
     * @return 帖子数量
     */
    int countByForumId(@Param("forumId") Long forumId);

    /**
     * 查询用户帖子列表
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @return 帖子列表
     */
    IPage<PostListVO> selectUserPosts(Page<PostListVO> page, @Param("userId") Long userId);

    /**
     * 查询板块帖子列表
     *
     * @param page 分页参数
     * @param forumId 板块ID
     * @param query 查询条件
     * @return 帖子列表
     */
    IPage<PostListVO> selectForumPosts(Page<PostListVO> page, @Param("forumId") Long forumId, @Param("query") PostQueryDTO query);
}
