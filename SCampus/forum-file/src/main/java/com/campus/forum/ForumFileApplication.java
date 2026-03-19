package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 论坛文件服务启动类
 * 
 * 提供文件上传、下载、管理等功能
 * 
 * 主要功能：
 * 1. 文件上传（支持普通文件和图片）
 * 2. 文件下载
 * 3. 文件信息查询
 * 4. 文件删除
 * 5. 文件列表查询
 * 
 * 支持存储方式：
 * 1. 本地存储
 * 2. MinIO对象存储
 * 
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient      // 启用Nacos服务发现
@EnableFeignClients        // 启用OpenFeign服务调用
@EnableTransactionManagement  // 启用事务管理
@MapperScan("com.campus.forum.mapper")  // 扫描Mapper接口
public class ForumFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumFileApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  Forum File Service Started Successfully!");
        System.out.println("  Port: 9010");
        System.out.println("  API Docs: http://localhost:9010/doc.html");
        System.out.println("==========================================");
    }
}
