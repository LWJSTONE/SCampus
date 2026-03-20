package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 审核服务启动类
 *
 * 提供举报、审核、禁言等功能
 *
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.campus.forum.api")
@MapperScan("com.campus.forum.mapper")
@EnableTransactionManagement
public class ForumReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumReportApplication.class, args);
        System.out.println("==========================================");
        System.out.println("    Forum Report Service Started!");
        System.out.println("    Port: 9007");
        System.out.println("==========================================");
    }
}
