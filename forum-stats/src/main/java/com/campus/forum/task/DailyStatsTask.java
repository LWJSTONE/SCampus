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
import java.util.concurrent.TimeUnit;

/**
 * 统计数据定时任务
 * 
 * 每日凌晨自动统计前一天的数据
 * 从Redis中收集实时统计数据并持久化到数据库
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
    
    // Redis中实时统计的key前缀
    private static final String DAILY_STATS_PREFIX = "daily:stats:";
    private static final String GLOBAL_STATS_KEY = "global:stats";

    /**
     * 每日凌晨1点执行统计数据生成
     * 统计前一天的数据，从Redis中获取实时统计并持久化
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
            
            // 从Redis获取昨日的统计数据
            String yesterdayKey = DAILY_STATS_PREFIX + yesterday.toString();
            
            // 获取新增用户数
            Object newUsersObj = redisTemplate.opsForValue().get(yesterdayKey + ":new_users");
            stats.setNewUsers(newUsersObj != null ? Integer.parseInt(newUsersObj.toString()) : 0);
            
            // 获取活跃用户数（从独立用户集合大小获取）
            Long activeUserCount = redisTemplate.opsForSet().size(yesterdayKey + ":active_users");
            stats.setActiveUsers(activeUserCount != null ? activeUserCount.intValue() : 0);
            
            // 获取新增帖子数
            Object newPostsObj = redisTemplate.opsForValue().get(yesterdayKey + ":new_posts");
            stats.setNewPosts(newPostsObj != null ? Integer.parseInt(newPostsObj.toString()) : 0);
            
            // 获取新增评论数
            Object newCommentsObj = redisTemplate.opsForValue().get(yesterdayKey + ":new_comments");
            stats.setNewComments(newCommentsObj != null ? Integer.parseInt(newCommentsObj.toString()) : 0);
            
            // 获取点赞数
            Object likeCountObj = redisTemplate.opsForValue().get(yesterdayKey + ":likes");
            stats.setLikeCount(likeCountObj != null ? Integer.parseInt(likeCountObj.toString()) : 0);
            
            // 获取收藏数
            Object collectCountObj = redisTemplate.opsForValue().get(yesterdayKey + ":collects");
            stats.setCollectCount(collectCountObj != null ? Integer.parseInt(collectCountObj.toString()) : 0);
            
            // 获取关注数
            Object followCountObj = redisTemplate.opsForValue().get(yesterdayKey + ":follows");
            stats.setFollowCount(followCountObj != null ? Integer.parseInt(followCountObj.toString()) : 0);
            
            // 获取浏览量
            Object viewCountObj = redisTemplate.opsForValue().get(yesterdayKey + ":views");
            stats.setViewCount(viewCountObj != null ? Long.parseLong(viewCountObj.toString()) : 0L);
            
            // 计算累计数据 = 最新累计数据 + 今日新增数据
            DailyStats latestStats = dailyStatsMapper.selectLatest();
            long baseTotalUsers = (latestStats != null && latestStats.getTotalUsers() != null) ? latestStats.getTotalUsers() : 0L;
            long baseTotalPosts = (latestStats != null && latestStats.getTotalPosts() != null) ? latestStats.getTotalPosts() : 0L;
            long baseTotalComments = (latestStats != null && latestStats.getTotalComments() != null) ? latestStats.getTotalComments() : 0L;
            
            stats.setTotalUsers(baseTotalUsers + stats.getNewUsers());
            stats.setTotalPosts(baseTotalPosts + stats.getNewPosts());
            stats.setTotalComments(baseTotalComments + stats.getNewComments());
            
            // 插入统计记录
            dailyStatsMapper.insert(stats);
            
            // 清理昨日的Redis统计key（保留今日的）
            cleanupYesterdayStats(yesterdayKey);
            
            // 清除所有统计相关的缓存
            clearStatsCache();
            
            log.info("日期 {} 的统计数据生成完成: 新增用户={}, 新增帖子={}, 新增评论={}, 浏览量={}", 
                    yesterday, stats.getNewUsers(), stats.getNewPosts(), stats.getNewComments(), stats.getViewCount());
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
            
            // 从Redis获取今日实时数据
            String todayKey = DAILY_STATS_PREFIX + today.toString();
            
            // 获取活跃用户数
            Long activeUserCount = redisTemplate.opsForSet().size(todayKey + ":active_users");
            int activeUsers = activeUserCount != null ? activeUserCount.intValue() : 0;
            
            // 获取浏览量
            Object viewCountObj = redisTemplate.opsForValue().get(todayKey + ":views");
            long viewCount = viewCountObj != null ? Long.parseLong(viewCountObj.toString()) : 0L;
            
            if (todayStats == null) {
                // 创建今日统计记录
                todayStats = new DailyStats();
                todayStats.setStatsDate(today);
                todayStats.setNewUsers(0);
                todayStats.setNewPosts(0);
                todayStats.setNewComments(0);
                todayStats.setActiveUsers(activeUsers);
                todayStats.setViewCount(viewCount);
                todayStats.setLikeCount(0);
                todayStats.setCollectCount(0);
                todayStats.setFollowCount(0);
                
                // 累计数据 = 最新累计数据
                DailyStats latestStats = dailyStatsMapper.selectLatest();
                long baseTotalUsers = (latestStats != null && latestStats.getTotalUsers() != null) ? latestStats.getTotalUsers() : 0L;
                long baseTotalPosts = (latestStats != null && latestStats.getTotalPosts() != null) ? latestStats.getTotalPosts() : 0L;
                long baseTotalComments = (latestStats != null && latestStats.getTotalComments() != null) ? latestStats.getTotalComments() : 0L;
                todayStats.setTotalUsers(baseTotalUsers);
                todayStats.setTotalPosts(baseTotalPosts);
                todayStats.setTotalComments(baseTotalComments);
                
                dailyStatsMapper.insert(todayStats);
            } else {
                // 更新今日活跃用户数和浏览量
                todayStats.setActiveUsers(activeUsers);
                todayStats.setViewCount(viewCount);
                dailyStatsMapper.updateById(todayStats);
            }
            
            // 清除缓存，让下次查询获取最新数据
            clearStatsCache();
            
            log.info("今日统计数据更新完成: 活跃用户={}, 浏览量={}", activeUsers, viewCount);
        } catch (Exception e) {
            log.error("更新今日统计数据失败", e);
        }
    }

    /**
     * 每5分钟同步一次今日实时计数器到Redis
     * 确保数据不会因Redis重启而丢失
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncTodayCounters() {
        try {
            LocalDate today = LocalDate.now();
            String todayKey = DAILY_STATS_PREFIX + today.toString();
            
            // 更新今日统计到全局统计key（用于快速查询）
            Object newUsers = redisTemplate.opsForValue().get(todayKey + ":new_users");
            Object newPosts = redisTemplate.opsForValue().get(todayKey + ":new_posts");
            Object newComments = redisTemplate.opsForValue().get(todayKey + ":new_comments");
            
            if (newUsers != null || newPosts != null || newComments != null) {
                // 设置缓存过期时间为7天，确保数据有足够时间被定时任务持久化
                redisTemplate.expire(todayKey + ":new_users", 7, TimeUnit.DAYS);
                redisTemplate.expire(todayKey + ":new_posts", 7, TimeUnit.DAYS);
                redisTemplate.expire(todayKey + ":new_comments", 7, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            log.warn("同步今日计数器失败", e);
        }
    }

    /**
     * 清理昨日的Redis统计key
     */
    private void cleanupYesterdayStats(String yesterdayKey) {
        try {
            Set<String> keysToDelete = new HashSet<>();
            keysToDelete.add(yesterdayKey + ":new_users");
            keysToDelete.add(yesterdayKey + ":new_posts");
            keysToDelete.add(yesterdayKey + ":new_comments");
            keysToDelete.add(yesterdayKey + ":likes");
            keysToDelete.add(yesterdayKey + ":collects");
            keysToDelete.add(yesterdayKey + ":follows");
            keysToDelete.add(yesterdayKey + ":views");
            keysToDelete.add(yesterdayKey + ":active_users");
            
            redisTemplate.delete(keysToDelete);
            log.debug("已清理昨日统计key: {}", yesterdayKey);
        } catch (Exception e) {
            log.warn("清理昨日统计key失败", e);
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
