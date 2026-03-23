package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Mention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @提及Mapper接口
 * 
 * 数据库表: forum_mention
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface MentionMapper extends BaseMapper<Mention> {

    /**
     * 获取用户的未读提及数
     * 查询被@的用户(to_user_id)的未读提及
     */
    @Select("SELECT COUNT(*) FROM forum_mention WHERE to_user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 获取用户的提及列表
     * 查询被@的用户(to_user_id)的所有提及
     */
    @Select("SELECT * FROM forum_mention WHERE to_user_id = #{userId} ORDER BY create_time DESC")
    List<Mention> selectByUserId(@Param("userId") Long userId);

    /**
     * 标记为已读（带归属权验证）
     * 只允许被@的用户(to_user_id)标记已读
     */
    @Update("UPDATE forum_mention SET is_read = 1 WHERE id = #{id} AND to_user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 标记所有为已读
     * 将用户所有未读提及标记为已读
     */
    @Update("UPDATE forum_mention SET is_read = 1 WHERE to_user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 检查是否已经提及过该用户（防止重复提及）
     */
    @Select("SELECT COUNT(*) FROM forum_mention WHERE from_user_id = #{fromUserId} " +
            "AND to_user_id = #{toUserId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int countExistingMention(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId,
                             @Param("targetType") Integer targetType, @Param("targetId") Long targetId);
}
