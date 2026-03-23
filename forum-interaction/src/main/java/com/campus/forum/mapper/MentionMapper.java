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
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface MentionMapper extends BaseMapper<Mention> {

    /**
     * 获取用户的未读提及数
     */
    @Select("SELECT COUNT(*) FROM t_mention WHERE user_id = #{userId} AND is_read = 0 AND delete_flag = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 获取用户的提及列表
     */
    @Select("SELECT * FROM t_mention WHERE user_id = #{userId} AND delete_flag = 0 ORDER BY create_time DESC")
    List<Mention> selectByUserId(@Param("userId") Long userId);

    /**
     * 标记为已读（带归属权验证）
     */
    @Update("UPDATE t_mention SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 标记所有为已读
     */
    @Update("UPDATE t_mention SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
