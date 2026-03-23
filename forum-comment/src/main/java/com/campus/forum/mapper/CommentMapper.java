package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.entity.Comment;
import com.campus.forum.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 评论Mapper接口
 * 
 * 提供评论数据访问操作
 * 注意：评论状态(status)定义 - 0:删除, 1:正常, 2:屏蔽
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询帖子的一级评论列表
     * 
     * @param page 分页参数
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID（用于判断是否点赞）
     * @return 评论列表
     */
    @Select("<script>" +
            "SELECT c.*, " +
            "       u.nickname as user_name, " +
            "       u.avatar as user_avatar, " +
            "       u.experience as user_level, " +
            "       ru.nickname as reply_to_user_name, " +
            "       CASE WHEN cl.id IS NOT NULL THEN 1 ELSE 0 END as is_liked " +
            "FROM forum_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "LEFT JOIN sys_user ru ON c.reply_user_id = ru.id " +
            "<if test='currentUserId != null'>" +
            "LEFT JOIN forum_like cl ON c.id = cl.target_id AND cl.target_type = 2 AND cl.user_id = #{currentUserId} AND cl.delete_flag = 0 " +
            "</if>" +
            "WHERE c.post_id = #{postId} " +
            "AND c.parent_id = 0 " +
            "AND c.delete_flag = 0 " +
            "AND c.status = 1 " +
            "ORDER BY c.is_hot DESC, c.create_time DESC" +
            "</script>")
    IPage<CommentVO> selectCommentsByPostId(Page<CommentVO> page, 
                                             @Param("postId") Long postId,
                                             @Param("currentUserId") Long currentUserId);

    /**
     * 查询评论的回复列表
     * 
     * @param parentId 父评论ID
     * @param currentUserId 当前用户ID
     * @param limit 限制数量
     * @return 回复列表
     */
    @Select("<script>" +
            "SELECT c.*, " +
            "       u.nickname as user_name, " +
            "       u.avatar as user_avatar, " +
            "       u.experience as user_level, " +
            "       ru.nickname as reply_to_user_name, " +
            "       CASE WHEN cl.id IS NOT NULL THEN 1 ELSE 0 END as is_liked " +
            "FROM forum_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "LEFT JOIN sys_user ru ON c.reply_user_id = ru.id " +
            "<if test='currentUserId != null'>" +
            "LEFT JOIN forum_like cl ON c.id = cl.target_id AND cl.target_type = 2 AND cl.user_id = #{currentUserId} AND cl.delete_flag = 0 " +
            "</if>" +
            "WHERE c.parent_id = #{parentId} " +
            "AND c.delete_flag = 0 " +
            "AND c.status = 1 " +
            "ORDER BY c.create_time ASC " +
            "LIMIT #{limit}" +
            "</script>")
    List<CommentVO> selectRepliesByParentId(@Param("parentId") Long parentId,
                                             @Param("currentUserId") Long currentUserId,
                                             @Param("limit") Integer limit);

    /**
     * 分页查询评论的回复列表
     * 
     * @param page 分页参数
     * @param parentId 父评论ID
     * @param currentUserId 当前用户ID
     * @return 回复列表
     */
    @Select("<script>" +
            "SELECT c.*, " +
            "       u.nickname as user_name, " +
            "       u.avatar as user_avatar, " +
            "       u.experience as user_level, " +
            "       ru.nickname as reply_to_user_name, " +
            "       CASE WHEN cl.id IS NOT NULL THEN 1 ELSE 0 END as is_liked " +
            "FROM forum_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "LEFT JOIN sys_user ru ON c.reply_user_id = ru.id " +
            "<if test='currentUserId != null'>" +
            "LEFT JOIN forum_like cl ON c.id = cl.target_id AND cl.target_type = 2 AND cl.user_id = #{currentUserId} AND cl.delete_flag = 0 " +
            "</if>" +
            "WHERE c.parent_id = #{parentId} " +
            "AND c.delete_flag = 0 " +
            "AND c.status = 1 " +
            "ORDER BY c.create_time ASC" +
            "</script>")
    IPage<CommentVO> selectRepliesPage(Page<CommentVO> page,
                                        @Param("parentId") Long parentId,
                                        @Param("currentUserId") Long currentUserId);

    /**
     * 查询用户的评论列表
     * 
     * @param page 分页参数
     * @param userId 用户ID
     * @param currentUserId 当前用户ID（用于判断是否点赞）
     * @return 评论列表
     */
    @Select("<script>" +
            "SELECT c.*, " +
            "       p.title as post_title, " +
            "       u.nickname as user_name, " +
            "       u.avatar as user_avatar, " +
            "       u.experience as user_level, " +
            "       ru.nickname as reply_to_user_name, " +
            "       CASE WHEN cl.id IS NOT NULL THEN 1 ELSE 0 END as is_liked " +
            "FROM forum_comment c " +
            "LEFT JOIN forum_post p ON c.post_id = p.id " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "LEFT JOIN sys_user ru ON c.reply_user_id = ru.id " +
            "<if test='currentUserId != null'>" +
            "LEFT JOIN forum_like cl ON c.id = cl.target_id AND cl.target_type = 2 AND cl.user_id = #{currentUserId} AND cl.delete_flag = 0 " +
            "</if>" +
            "WHERE c.user_id = #{userId} " +
            "AND c.delete_flag = 0 " +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<CommentVO> selectCommentsByUserId(Page<CommentVO> page, 
                                            @Param("userId") Long userId,
                                            @Param("currentUserId") Long currentUserId);

    /**
     * 增加评论点赞数
     * 
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE forum_comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 减少评论点赞数
     * 
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE forum_comment SET like_count = GREATEST(0, like_count - 1) WHERE id = #{commentId}")
    int decrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 增加评论回复数
     * 
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE forum_comment SET reply_count = reply_count + 1 WHERE id = #{commentId}")
    int incrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 减少评论回复数
     * 
     * @param commentId 评论ID
     * @return 影响行数
     */
    @Update("UPDATE forum_comment SET reply_count = GREATEST(0, reply_count - 1) WHERE id = #{commentId}")
    int decrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 统计帖子评论总数
     * 
     * @param postId 帖子ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM forum_comment WHERE post_id = #{postId} AND delete_flag = 0 AND status = 1")
    int countByPostId(@Param("postId") Long postId);

    /**
     * 批量统计帖子评论总数（优化N+1查询）
     * 
     * @param postIds 帖子ID列表
     * @return 帖子ID与评论数量的映射
     */
    @Select("<script>" +
            "SELECT post_id, COUNT(*) as count FROM forum_comment " +
            "WHERE post_id IN " +
            "<foreach collection='postIds' item='postId' open='(' separator=',' close=')'>" +
            "    #{postId}" +
            "</foreach>" +
            "AND delete_flag = 0 AND status = 1 " +
            "GROUP BY post_id" +
            "</script>")
    List<Map<String, Object>> countByPostIds(@Param("postIds") List<Long> postIds);

    /**
     * 更新评论热门状态
     * 
     * @param commentId 评论ID
     * @param isHot 是否热门
     * @return 影响行数
     */
    @Update("UPDATE forum_comment SET is_hot = #{isHot} WHERE id = #{commentId}")
    int updateHotStatus(@Param("commentId") Long commentId, @Param("isHot") Integer isHot);

    /**
     * 批量逻辑删除子评论
     * 
     * @param parentId 父评论ID
     * @return 影响行数
     */
    @Update("UPDATE forum_comment SET delete_flag = 1, status = 0 WHERE parent_id = #{parentId} AND delete_flag = 0")
    int deleteByParentId(@Param("parentId") Long parentId);
}
