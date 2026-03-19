package com.campus.forum.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.campus.forum.dto.StatsQueryDTO;
import com.campus.forum.entity.DailyStats;
import com.campus.forum.mapper.DailyStatsMapper;
import com.campus.forum.service.StatsService;
import com.campus.forum.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 统计服务实现类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final DailyStatsMapper dailyStatsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存key前缀
    private static final String CACHE_PREFIX = "stats:";
    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE = 300;

    @Override
    public OverviewStatsVO getOverview() {
        String cacheKey = CACHE_PREFIX + "overview";
        OverviewStatsVO cached = (OverviewStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        OverviewStatsVO vo = new OverviewStatsVO();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 获取最新统计数据
        DailyStats todayStats = dailyStatsMapper.selectByDate(today);
        DailyStats yesterdayStats = dailyStatsMapper.selectByDate(yesterday);
        DailyStats latestStats = dailyStatsMapper.selectLatest();

        if (latestStats != null) {
            vo.setTotalUsers(latestStats.getTotalUsers() != null ? latestStats.getTotalUsers() : 0L);
            vo.setTotalPosts(latestStats.getTotalPosts() != null ? latestStats.getTotalPosts() : 0L);
            vo.setTotalComments(latestStats.getTotalComments() != null ? latestStats.getTotalComments() : 0L);
        }

        if (todayStats != null) {
            vo.setTodayNewUsers(todayStats.getNewUsers() != null ? todayStats.getNewUsers() : 0);
            vo.setTodayNewPosts(todayStats.getNewPosts() != null ? todayStats.getNewPosts() : 0);
            vo.setTodayNewComments(todayStats.getNewComments() != null ? todayStats.getNewComments() : 0);
            vo.setTodayActiveUsers(todayStats.getActiveUsers() != null ? todayStats.getActiveUsers() : 0);
            vo.setTodayViews(todayStats.getViewCount() != null ? todayStats.getViewCount() : 0L);
        } else {
            vo.setTodayNewUsers(0);
            vo.setTodayNewPosts(0);
            vo.setTodayNewComments(0);
            vo.setTodayActiveUsers(0);
            vo.setTodayViews(0L);
        }

        // 计算增长率
        if (yesterdayStats != null && yesterdayStats.getNewPosts() != null && yesterdayStats.getNewPosts() > 0) {
            double postGrowth = ((vo.getTodayNewPosts() - yesterdayStats.getNewPosts()) * 100.0) / yesterdayStats.getNewPosts();
            vo.setPostGrowthRate(Math.round(postGrowth * 100.0) / 100.0);
        } else {
            vo.setPostGrowthRate(0.0);
        }

        if (yesterdayStats != null && yesterdayStats.getNewUsers() != null && yesterdayStats.getNewUsers() > 0) {
            double userGrowth = ((vo.getTodayNewUsers() - yesterdayStats.getNewUsers()) * 100.0) / yesterdayStats.getNewUsers();
            vo.setUserGrowthRate(Math.round(userGrowth * 100.0) / 100.0);
        } else {
            vo.setUserGrowthRate(0.0);
        }

        // 计算总浏览量（近30天）
        LocalDate monthStart = today.minusDays(30);
        Long totalViews = dailyStatsMapper.sumViewCount(monthStart, today);
        vo.setTotalViews(totalViews != null ? totalViews : 0L);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    public UserStatsVO getUserStats() {
        String cacheKey = CACHE_PREFIX + "user";
        UserStatsVO cached = (UserStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        UserStatsVO vo = new UserStatsVO();
        LocalDate today = LocalDate.now();

        // 获取最新统计数据
        DailyStats latestStats = dailyStatsMapper.selectLatest();
        if (latestStats != null) {
            vo.setTotalUsers(latestStats.getTotalUsers() != null ? latestStats.getTotalUsers() : 0L);
        } else {
            vo.setTotalUsers(0L);
        }

        // 今日数据
        DailyStats todayStats = dailyStatsMapper.selectByDate(today);
        if (todayStats != null) {
            vo.setTodayNewUsers(todayStats.getNewUsers() != null ? todayStats.getNewUsers() : 0);
            vo.setTodayActiveUsers(todayStats.getActiveUsers() != null ? todayStats.getActiveUsers() : 0);
        } else {
            vo.setTodayNewUsers(0);
            vo.setTodayActiveUsers(0);
        }

        // 本周数据
        LocalDate weekStart = today.minusDays(7);
        vo.setWeekNewUsers(dailyStatsMapper.sumNewUsers(weekStart, today));
        vo.setWeekActiveUsers(dailyStatsMapper.sumActiveUsers(weekStart, today));

        // 本月数据
        LocalDate monthStart = today.minusDays(30);
        vo.setMonthNewUsers(dailyStatsMapper.sumNewUsers(monthStart, today));
        vo.setMonthActiveUsers(dailyStatsMapper.sumActiveUsers(monthStart, today));

        // 计算增长率（对比上周）
        LocalDate lastWeekStart = weekStart.minusDays(7);
        Integer lastWeekNewUsers = dailyStatsMapper.sumNewUsers(lastWeekStart, weekStart);
        if (lastWeekNewUsers != null && lastWeekNewUsers > 0) {
            double growth = ((vo.getWeekNewUsers() - lastWeekNewUsers) * 100.0) / lastWeekNewUsers;
            vo.setGrowthRate(Math.round(growth * 100.0) / 100.0);
        } else {
            vo.setGrowthRate(0.0);
        }

        // 计算活跃率
        if (vo.getTotalUsers() > 0) {
            double activeRate = (vo.getTodayActiveUsers() * 100.0) / vo.getTotalUsers();
            vo.setActiveRate(Math.round(activeRate * 100.0) / 100.0);
        } else {
            vo.setActiveRate(0.0);
        }

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    public PostStatsVO getPostStats() {
        String cacheKey = CACHE_PREFIX + "post";
        PostStatsVO cached = (PostStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        PostStatsVO vo = new PostStatsVO();
        LocalDate today = LocalDate.now();

        // 获取最新统计数据
        DailyStats latestStats = dailyStatsMapper.selectLatest();
        if (latestStats != null) {
            vo.setTotalPosts(latestStats.getTotalPosts() != null ? latestStats.getTotalPosts() : 0L);
            vo.setTotalComments(latestStats.getTotalComments() != null ? latestStats.getTotalComments() : 0L);
        } else {
            vo.setTotalPosts(0L);
        }

        // 今日数据
        DailyStats todayStats = dailyStatsMapper.selectByDate(today);
        if (todayStats != null) {
            vo.setTodayNewPosts(todayStats.getNewPosts() != null ? todayStats.getNewPosts() : 0);
            vo.setTodayNewComments(todayStats.getNewComments() != null ? todayStats.getNewComments() : 0);
        } else {
            vo.setTodayNewPosts(0);
            vo.setTodayNewComments(0);
        }

        // 本周数据
        LocalDate weekStart = today.minusDays(7);
        vo.setWeekNewPosts(dailyStatsMapper.sumNewPosts(weekStart, today));

        // 本月数据
        LocalDate monthStart = today.minusDays(30);
        vo.setMonthNewPosts(dailyStatsMapper.sumNewPosts(monthStart, today));

        // 计算增长率
        LocalDate lastWeekStart = weekStart.minusDays(7);
        Integer lastWeekNewPosts = dailyStatsMapper.sumNewPosts(lastWeekStart, weekStart);
        if (lastWeekNewPosts != null && lastWeekNewPosts > 0) {
            double growth = ((vo.getWeekNewPosts() - lastWeekNewPosts) * 100.0) / lastWeekNewPosts;
            vo.setGrowthRate(Math.round(growth * 100.0) / 100.0);
        } else {
            vo.setGrowthRate(0.0);
        }

        // 计算平均值
        if (vo.getTotalPosts() > 0 && vo.getTotalComments() > 0) {
            vo.setAvgCommentsPerPost(Math.round(vo.getTotalComments() * 100.0 / vo.getTotalPosts()) / 100.0);
        } else {
            vo.setAvgCommentsPerPost(0.0);
        }

        // 模拟其他数据（实际应从其他服务获取）
        vo.setPendingPosts(0);
        vo.setPublishedPosts(vo.getTodayNewPosts());
        vo.setDeletedPosts(0);
        vo.setAvgLikesPerPost(0.0);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    public InteractionStatsVO getInteractionStats() {
        String cacheKey = CACHE_PREFIX + "interaction";
        InteractionStatsVO cached = (InteractionStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        InteractionStatsVO vo = new InteractionStatsVO();
        LocalDate today = LocalDate.now();

        // 今日数据
        DailyStats todayStats = dailyStatsMapper.selectByDate(today);
        if (todayStats != null) {
            vo.setTodayLikes(todayStats.getLikeCount() != null ? todayStats.getLikeCount() : 0);
            vo.setTodayComments(todayStats.getNewComments() != null ? todayStats.getNewComments() : 0);
            vo.setTodayCollects(todayStats.getCollectCount() != null ? todayStats.getCollectCount() : 0);
            vo.setTodayFollows(todayStats.getFollowCount() != null ? todayStats.getFollowCount() : 0);
            vo.setTodayViews(todayStats.getViewCount() != null ? todayStats.getViewCount() : 0L);
        } else {
            vo.setTodayLikes(0);
            vo.setTodayComments(0);
            vo.setTodayCollects(0);
            vo.setTodayFollows(0);
            vo.setTodayViews(0L);
        }

        // 计算近30天总数据
        LocalDate monthStart = today.minusDays(30);
        vo.setTotalLikes(dailyStatsMapper.sumLikeCount(monthStart, today).longValue());
        vo.setTotalComments(dailyStatsMapper.sumNewComments(monthStart, today).longValue());
        vo.setTotalCollects(dailyStatsMapper.sumCollectCount(monthStart, today).longValue());
        vo.setTotalFollows(dailyStatsMapper.sumFollowCount(monthStart, today).longValue());
        vo.setTotalViews(dailyStatsMapper.sumViewCount(monthStart, today));

        // 计算增长率
        LocalDate lastWeekStart = monthStart.minusDays(7);
        Integer lastWeekLikes = dailyStatsMapper.sumLikeCount(lastWeekStart, monthStart);
        Integer thisWeekLikes = dailyStatsMapper.sumLikeCount(monthStart, today);
        if (lastWeekLikes != null && lastWeekLikes > 0) {
            double growth = ((thisWeekLikes - lastWeekLikes) * 100.0) / lastWeekLikes;
            vo.setGrowthRate(Math.round(growth * 100.0) / 100.0);
        } else {
            vo.setGrowthRate(0.0);
        }

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    public TrendDataVO getTrend(String rangeType) {
        String cacheKey = CACHE_PREFIX + "trend:" + rangeType;
        TrendDataVO cached = (TrendDataVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        TrendDataVO vo = new TrendDataVO();
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        int days;

        // 根据范围类型确定日期范围
        if ("week".equals(rangeType)) {
            startDate = today.minusDays(7);
            days = 7;
        } else if ("month".equals(rangeType)) {
            startDate = today.minusDays(30);
            days = 30;
        } else {
            // 默认近7天
            startDate = today.minusDays(7);
            days = 7;
        }

        // 查询日期范围内的统计数据
        List<DailyStats> statsList = dailyStatsMapper.selectByDateRange(startDate, today);

        // 构建返回数据
        List<String> dates = new ArrayList<>();
        List<Integer> userTrend = new ArrayList<>();
        List<Integer> postTrend = new ArrayList<>();
        List<Integer> commentTrend = new ArrayList<>();
        List<Long> viewTrend = new ArrayList<>();
        List<Integer> activeUserTrend = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));

            // 查找对应日期的数据
            DailyStats dayStats = statsList.stream()
                    .filter(s -> s.getStatsDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (dayStats != null) {
                userTrend.add(dayStats.getNewUsers() != null ? dayStats.getNewUsers() : 0);
                postTrend.add(dayStats.getNewPosts() != null ? dayStats.getNewPosts() : 0);
                commentTrend.add(dayStats.getNewComments() != null ? dayStats.getNewComments() : 0);
                viewTrend.add(dayStats.getViewCount() != null ? dayStats.getViewCount() : 0L);
                activeUserTrend.add(dayStats.getActiveUsers() != null ? dayStats.getActiveUsers() : 0);
            } else {
                userTrend.add(0);
                postTrend.add(0);
                commentTrend.add(0);
                viewTrend.add(0L);
                activeUserTrend.add(0);
            }
        }

        vo.setDates(dates);
        vo.setUserTrend(userTrend);
        vo.setPostTrend(postTrend);
        vo.setCommentTrend(commentTrend);
        vo.setViewTrend(viewTrend);
        vo.setActiveUserTrend(activeUserTrend);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    public void exportReport(String statsType, HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("统计报表_" + DateUtil.today(), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 根据类型导出不同数据
            if ("user".equals(statsType)) {
                exportUserReport(response);
            } else if ("post".equals(statsType)) {
                exportPostReport(response);
            } else if ("interaction".equals(statsType)) {
                exportInteractionReport(response);
            } else {
                exportOverviewReport(response);
            }
        } catch (IOException e) {
            log.error("导出报表失败", e);
            throw new RuntimeException("导出报表失败: " + e.getMessage());
        }
    }

    private void exportOverviewReport(HttpServletResponse response) throws IOException {
        List<OverviewStatsVO> dataList = new ArrayList<>();
        dataList.add(getOverview());

        EasyExcel.write(response.getOutputStream(), OverviewStatsVO.class)
                .sheet("系统概览统计")
                .doWrite(dataList);
    }

    private void exportUserReport(HttpServletResponse response) throws IOException {
        List<UserStatsVO> dataList = new ArrayList<>();
        dataList.add(getUserStats());

        EasyExcel.write(response.getOutputStream(), UserStatsVO.class)
                .sheet("用户统计")
                .doWrite(dataList);
    }

    private void exportPostReport(HttpServletResponse response) throws IOException {
        List<PostStatsVO> dataList = new ArrayList<>();
        dataList.add(getPostStats());

        EasyExcel.write(response.getOutputStream(), PostStatsVO.class)
                .sheet("帖子统计")
                .doWrite(dataList);
    }

    private void exportInteractionReport(HttpServletResponse response) throws IOException {
        List<InteractionStatsVO> dataList = new ArrayList<>();
        dataList.add(getInteractionStats());

        EasyExcel.write(response.getOutputStream(), InteractionStatsVO.class)
                .sheet("互动统计")
                .doWrite(dataList);
    }
}
