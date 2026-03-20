package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 帖子服务启动类
 * 提供帖子的发布、管理、查询等核心功能
 *
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.campus.forum.api")
@MapperScan("com.campus.forum.mapper")
@EnableTransactionManagement
public class ForumPostApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumPostApplication.class, args);
        System.out.println("==========================================");
        System.out.println("     帖子服务启动成功！端口：9004");
        System.out.println("==========================================");
    }
}
