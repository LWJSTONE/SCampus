package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 论坛评论服务启动类
 * 
 * 提供帖子评论、回复、点赞等功能
 * 
 * 主要功能：
 * 1. 发布评论（支持楼中楼）
 * 2. 删除评论
 * 3. 评论点赞
 * 4. 获取评论列表
 * 5. 获取评论回复列表
 * 
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient      // 启用Nacos服务发现
@EnableFeignClients        // 启用OpenFeign服务调用
@EnableTransactionManagement  // 启用事务管理
@MapperScan("com.campus.forum.mapper")  // 扫描Mapper接口
public class ForumCommentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumCommentApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  Forum Comment Service Started Successfully!");
        System.out.println("  Port: 9005");
        System.out.println("  API Docs: http://localhost:9005/doc.html");
        System.out.println("==========================================");
    }
}
