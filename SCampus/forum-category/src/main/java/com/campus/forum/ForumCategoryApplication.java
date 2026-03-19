package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 版块服务启动类
 *
 * 版块服务提供以下功能：
 * - 版块分类管理（树形结构）
 * - 版块管理（创建、更新、删除）
 * - 版主管理（添加、移除）
 *
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.campus.forum.api")
@MapperScan("com.campus.forum.mapper")
public class ForumCategoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumCategoryApplication.class, args);
        System.out.println("==========================================");
        System.out.println("    Forum Category Service Started!       ");
        System.out.println("    版块服务启动成功，端口：9003            ");
        System.out.println("==========================================");
    }
}
