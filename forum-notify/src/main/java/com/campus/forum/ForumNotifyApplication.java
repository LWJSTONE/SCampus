package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 论坛通知服务启动类
 * 
 * 提供系统通知、消息推送等功能
 * 
 * 主要功能：
 * 1. 发布系统通知公告
 * 2. 用户消息通知
 * 3. 未读消息统计
 * 4. 消息已读标记
 * 
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableFeignClients        // 启用OpenFeign服务调用
@EnableTransactionManagement  // 启用事务管理
@MapperScan("com.campus.forum.mapper")  // 扫描Mapper接口
public class ForumNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumNotifyApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  Forum Notify Service Started Successfully!");
        System.out.println("  Port: 9009");
        System.out.println("  API Docs: http://localhost:9009/doc.html");
        System.out.println("==========================================");
    }
}
