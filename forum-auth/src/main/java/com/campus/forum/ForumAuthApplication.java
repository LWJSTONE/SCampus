package com.campus.forum;

import com.campus.forum.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

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
@EnableFeignClients(basePackages = "com.campus.forum.api")
@MapperScan("com.campus.forum.mapper")
@ComponentScan(
    basePackages = "com.campus.forum",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class)
    }
)
public class ForumAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumAuthApplication.class, args);
        System.out.println("========================================");
        System.out.println("    认证授权服务启动成功！端口：9001");
        System.out.println("    API文档地址：http://localhost:9001/doc.html");
        System.out.println("========================================");
    }
}
