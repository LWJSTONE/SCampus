package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 * 
 * 用户服务提供以下功能：
 * - 用户信息管理（查询、更新、头像上传等）
 * - 用户关注/取消关注
 * - 用户粉丝/关注列表
 * - 用户帖子、评论、收藏查询
 * 
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
// 开启Feign客户端，扫描forum-api模块中的Feign接口
@EnableFeignClients(basePackages = "com.campus.forum.api")
// 扫描Mapper接口
@MapperScan("com.campus.forum.mapper")
public class ForumUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumUserApplication.class, args);
        System.out.println("==========================================");
        System.out.println("     Forum User Service Started!         ");
        System.out.println("     用户服务启动成功，端口：9002           ");
        System.out.println("==========================================");
    }
}
