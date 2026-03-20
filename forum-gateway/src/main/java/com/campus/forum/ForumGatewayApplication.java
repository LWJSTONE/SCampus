package com.campus.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Forum Gateway 应用启动类
 * 
 * <p>API网关服务，作为系统的统一入口，提供以下功能：</p>
 * <ul>
 *     <li>路由转发：将请求转发到对应的微服务</li>
 *     <li>认证鉴权：JWT Token验证，统一身份认证</li>
 *     <li>限流熔断：基于Sentinel的流量控制和熔断降级</li>
 *     <li>跨域处理：统一CORS配置</li>
 *     <li>日志记录：请求日志统一记录</li>
 * </ul>
 * 
 * <p>注意：Gateway使用WebFlux响应式编程，不兼容Servlet的@RestControllerAdvice</p>
 * <p>因此需要排除forum-common中的Servlet异常处理器</p>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(
    basePackages = {
        "com.campus.forum",
        "com.alibaba.nacos",
        "com.alibaba.cloud"
    },
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.campus\\.forum\\.exception\\..*"
        )
    }
)
public class ForumGatewayApplication {

    /**
     * 应用程序入口
     * 
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ForumGatewayApplication.class, args);
        System.out.println("==========================================");
        System.out.println("    Forum Gateway Started Successfully    ");
        System.out.println("    Gateway Port: 8080                   ");
        System.out.println("==========================================");
    }

}
