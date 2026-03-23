package com.campus.forum.task;

import com.campus.forum.entity.DailyStats;
import com.campus.forum.mapper.DailyStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 统计数据定时任务
 * 
 * 每日凌晨自动统计前一天的数据
 * 
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatsTask {

    private final DailyStatsMapper dailyStatsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "stats:";

    /**
     * 每日凌晨1点执行统计数据生成
     * 统计前一天的数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyStats() {
        log.info("开始执行每日统计任务...");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // 检查是否已存在该日期的统计数据
            DailyStats existingStats = dailyStatsMapper.selectByDate(yesterday);
            if (existingStats != null) {
                log.info("日期 {} 的统计数据已存在，跳过生成", yesterday);
                return;
            }
            
            // 创建新的统计记录
            DailyStats stats = new DailyStats();
            stats.setStatsDate(yesterday);
            
            // TODO: 实际项目中应该从各业务服务获取真实数据
            // 这里初始化为0，等待后续从其他服务同步数据
            stats.setNewUsers(0);
            stats.setNewPosts(0);
            stats.setNewComments(0);
            stats.setActiveUsers(0);
            stats.setViewCount(0L);
            stats.setLikeCount(0);
            stats.setCollectCount(0);
            stats.setFollowCount(0);
            
            // 累计数据 = 最新累计数据 + 今日新增数据
            DailyStats latestStats = dailyStatsMapper.selectLatest();
            long baseTotalUsers = (latestStats != null && latestStats.getTotalUsers() != null) ? latestStats.getTotalUsers() : 0L;
            long baseTotalPosts = (latestStats != null && latestStats.getTotalPosts() != null) ? latestStats.getTotalPosts() : 0L;
            long baseTotalComments = (latestStats != null && latestStats.getTotalComments() != null) ? latestStats.getTotalComments() : 0L;
            stats.setTotalUsers(baseTotalUsers + (stats.getNewUsers() != null ? stats.getNewUsers() : 0));
            stats.setTotalPosts(baseTotalPosts + (stats.getNewPosts() != null ? stats.getNewPosts() : 0));
            stats.setTotalComments(baseTotalComments + (stats.getNewComments() != null ? stats.getNewComments() : 0));
            
            // 插入统计记录
            dailyStatsMapper.insert(stats);
            
            // 清除所有统计相关的缓存
            clearStatsCache();
            
            log.info("日期 {} 的统计数据生成完成", yesterday);
        } catch (Exception e) {
            log.error("生成每日统计数据失败", e);
        }
    }

    /**
     * 每小时更新一次今日实时统计
     * 用于更新累计数据和活跃用户数
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateTodayStats() {
        log.info("开始更新今日统计数据...");
        
        try {
            LocalDate today = LocalDate.now();
            
            // 检查今日统计记录是否存在
            DailyStats todayStats = dailyStatsMapper.selectByDate(today);
            if (todayStats == null) {
                // 创建今日统计记录
                todayStats = new DailyStats();
                todayStats.setStatsDate(today);
                todayStats.setNewUsers(0);
                todayStats.setNewPosts(0);
                todayStats.setNewComments(0);
                todayStats.setActiveUsers(0);
                todayStats.setViewCount(0L);
                todayStats.setLikeCount(0);
                todayStats.setCollectCount(0);
                todayStats.setFollowCount(0);
                
                // 累计数据 = 最新累计数据 + 今日新增数据
                DailyStats latestStats = dailyStatsMapper.selectLatest();
                long baseTotalUsers = (latestStats != null && latestStats.getTotalUsers() != null) ? latestStats.getTotalUsers() : 0L;
                long baseTotalPosts = (latestStats != null && latestStats.getTotalPosts() != null) ? latestStats.getTotalPosts() : 0L;
                long baseTotalComments = (latestStats != null && latestStats.getTotalComments() != null) ? latestStats.getTotalComments() : 0L;
                todayStats.setTotalUsers(baseTotalUsers + (todayStats.getNewUsers() != null ? todayStats.getNewUsers() : 0));
                todayStats.setTotalPosts(baseTotalPosts + (todayStats.getNewPosts() != null ? todayStats.getNewPosts() : 0));
                todayStats.setTotalComments(baseTotalComments + (todayStats.getNewComments() != null ? todayStats.getNewComments() : 0));
                
                dailyStatsMapper.insert(todayStats);
            }
            
            // 清除缓存，让下次查询获取最新数据
            clearStatsCache();
            
            log.info("今日统计数据更新完成");
        } catch (Exception e) {
            log.error("更新今日统计数据失败", e);
        }
    }

    /**
     * 清除统计相关缓存
     * 使用SCAN命令替代keys()，避免在生产环境造成Redis阻塞
     */
    private void clearStatsCache() {
        try {
            // 使用SCAN命令增量遍历键，避免keys()阻塞Redis
            Set<String> keys = new HashSet<>();
            Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection()
                    .scan(ScanOptions.scanOptions().match(CACHE_PREFIX + "*").count(100).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除 {} 个统计缓存", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除统计缓存失败", e);
        }
    }
}
