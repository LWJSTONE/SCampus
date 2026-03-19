package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 统计服务启动类
 * 
 * 统计服务提供以下功能：
 * - 系统概览统计（用户数、帖子数、评论数等）
 * - 用户统计（新增用户、活跃用户等）
 * - 帖子统计（发帖量、热门帖子等）
 * - 互动统计（点赞、评论、收藏等）
 * - 趋势数据（日/周/月趋势）
 * - 报表导出（Excel导出）
 * 
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.campus.forum.api")
@MapperScan("com.campus.forum.mapper")
@EnableScheduling
public class ForumStatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumStatsApplication.class, args);
        System.out.println("==========================================");
        System.out.println("     Forum Stats Service Started!        ");
        System.out.println("     统计服务启动成功，端口：9008           ");
        System.out.println("==========================================");
    }
}
