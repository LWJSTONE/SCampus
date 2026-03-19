package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.DailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日统计Mapper接口
 * 
 * @author campus
 * @since 2024-01-01
 */
@Mapper
public interface DailyStatsMapper extends BaseMapper<DailyStats> {

    /**
     * 查询日期范围内的统计数据
     */
    @Select("SELECT * FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0 ORDER BY stats_date ASC")
    List<DailyStats> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取指定日期的统计数据
     */
    @Select("SELECT * FROM daily_stats WHERE stats_date = #{date} AND delete_flag = 0")
    DailyStats selectByDate(@Param("date") LocalDate date);

    /**
     * 获取最新统计数据
     */
    @Select("SELECT * FROM daily_stats WHERE delete_flag = 0 ORDER BY stats_date DESC LIMIT 1")
    DailyStats selectLatest();

    /**
     * 计算日期范围内的汇总数据
     */
    @Select("SELECT COALESCE(SUM(new_users), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumNewUsers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(new_posts), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumNewPosts(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(new_comments), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumNewComments(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(active_users), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumActiveUsers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumLikeCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(collect_count), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumCollectCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(follow_count), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Integer sumFollowCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM daily_stats WHERE stats_date BETWEEN #{startDate} AND #{endDate} AND delete_flag = 0")
    Long sumViewCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
