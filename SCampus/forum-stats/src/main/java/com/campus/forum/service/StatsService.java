package com.campus.forum.service;

import com.campus.forum.dto.StatsQueryDTO;
import com.campus.forum.vo.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 统计服务接口
 * 
 * @author campus
 * @since 2024-01-01
 */
public interface StatsService {

    /**
     * 获取系统概览统计
     */
    OverviewStatsVO getOverview();

    /**
     * 获取用户统计
     */
    UserStatsVO getUserStats();

    /**
     * 获取帖子统计
     */
    PostStatsVO getPostStats();

    /**
     * 获取互动统计
     */
    InteractionStatsVO getInteractionStats();

    /**
     * 获取趋势数据
     *
     * @param rangeType 时间范围类型：day-近7天，week-近4周，month-近12月
     */
    TrendDataVO getTrend(String rangeType);

    /**
     * 导出报表
     *
     * @param statsType 统计类型
     * @param response  HTTP响应
     */
    void exportReport(String statsType, HttpServletResponse response);
}
