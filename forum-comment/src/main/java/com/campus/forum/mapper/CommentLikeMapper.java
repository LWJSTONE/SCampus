package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评论点赞Mapper接口
 * 
 * 提供评论点赞数据访问操作
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    /**
     * 查询用户对评论的点赞记录
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞记录
     */
    @Select("SELECT * FROM t_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId} AND delete_flag = 0")
    CommentLike selectByCommentIdAndUserId(@Param("commentId") Long commentId, 
                                            @Param("userId") Long userId);

    /**
     * 查询用户是否已点赞（包括已取消的记录）
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞记录（包括已取消）
     */
    @Select("SELECT * FROM t_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId} LIMIT 1")
    CommentLike selectExistsRecord(@Param("commentId") Long commentId, 
                                    @Param("userId") Long userId);

    /**
     * 统计评论的点赞数
     * 
     * @param commentId 评论ID
     * @return 点赞数
     */
    @Select("SELECT COUNT(*) FROM t_comment_like WHERE comment_id = #{commentId} AND delete_flag = 0")
    int countByCommentId(@Param("commentId") Long commentId);
}
