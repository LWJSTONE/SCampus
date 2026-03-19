package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证授权服务启动类
 * 
 * <p>认证授权服务提供以下功能：</p>
 * <ul>
 *   <li>用户登录认证</li>
 *   <li>用户注册</li>
 *   <li>Token生成与刷新</li>
 *   <li>验证码生成与校验</li>
 *   <li>密码重置</li>
 *   <li>用户信息查询</li>
 * </ul>
 *
 * @author campus
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.campus.forum.api")
@MapperScan("com.campus.forum.mapper")
public class ForumAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumAuthApplication.class, args);
        System.out.println("========================================");
        System.out.println("    认证授权服务启动成功！端口：9001");
        System.out.println("    API文档地址：http://localhost:9001/doc.html");
        System.out.println("========================================");
    }
}
