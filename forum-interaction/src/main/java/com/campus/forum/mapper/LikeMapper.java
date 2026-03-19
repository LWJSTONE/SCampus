package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 点赞Mapper接口
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {

    /**
     * 统计目标的点赞数
     */
    @Select("SELECT COUNT(*) FROM t_like WHERE target_type = #{targetType} AND target_id = #{targetId} AND delete_flag = 0")
    int countByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 检查是否已点赞
     */
    @Select("SELECT COUNT(*) FROM t_like WHERE target_type = #{targetType} AND target_id = #{targetId} AND user_id = #{userId} AND delete_flag = 0")
    int checkLiked(@Param("targetType") Integer targetType, @Param("targetId") Long targetId, @Param("userId") Long userId);
}
