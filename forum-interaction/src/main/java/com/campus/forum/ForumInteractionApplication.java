package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 论坛互动服务启动类
 * 
 * 提供点赞、收藏、@提及等功能
 * 
 * 主要功能：
 * 1. 帖子点赞/取消点赞
 * 2. 帖子收藏/取消收藏
 * 3. @提及用户
 * 4. 获取收藏列表
 * 
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableTransactionManagement
@MapperScan("com.campus.forum.mapper")
public class ForumInteractionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumInteractionApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  Forum Interaction Service Started Successfully!");
        System.out.println("  Port: 9006");
        System.out.println("  API Docs: http://localhost:9006/doc.html");
        System.out.println("==========================================");
    }
}
