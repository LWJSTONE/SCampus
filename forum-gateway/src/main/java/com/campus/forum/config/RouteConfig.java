package com.campus.forum.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由配置类
 * 
 * <p>配置Spring Cloud Gateway的路由规则，定义请求如何转发到后端微服务。</p>
 * 
 * <p>路由规则说明：</p>
 * <ul>
 *     <li>认证服务 forum-auth：端口9001，路径 /api/v1/auth/**</li>
 *     <li>用户服务 forum-user：端口9002，路径 /api/v1/users/**</li>
 *     <li>分类服务 forum-category：端口9003，路径 /api/v1/categories/**</li>
 *     <li>帖子服务 forum-post：端口9004，路径 /api/v1/posts/**</li>
 *     <li>评论服务 forum-comment：端口9005，路径 /api/v1/comments/**</li>
 *     <li>交互服务 forum-interaction：端口9006，路径 /api/v1/interactions/**</li>
 *     <li>举报服务 forum-report：端口9007，路径 /api/v1/reports/**</li>
 *     <li>统计服务 forum-stats：端口9008，路径 /api/v1/stats/**</li>
 *     <li>通知服务 forum-notify：端口9009，路径 /api/v1/notices/**</li>
 *     <li>文件服务 forum-file：端口9010，路径 /api/v1/files/**</li>
 * </ul>
 * 
 * <p>注意：此配置类作为备份，主要路由规则在application.yml中配置。</p>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class RouteConfig {

    /**
     * 自定义路由定位器
     * 
     * <p>通过Java DSL方式配置路由规则，提供更灵活的路由配置能力。</p>
     * <p>此配置作为application.yml中路由配置的补充，优先级较低。</p>
     * 
     * @param builder 路由定位器构建器
     * @return RouteLocator 路由定位器
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由
                .route("forum-auth", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                // 添加请求头
                                .addRequestHeader("X-Gateway-Name", "forum-gateway")
                                // 请求日志
                                .filter((exchange, chain) -> {
                                    exchange.getRequest().mutate()
                                            .header("X-Request-Id", 
                                                    java.util.UUID.randomUUID().toString())
                                            .build();
                                    return chain.filter(exchange);
                                })
                        )
                        .uri("lb://forum-auth")
                )
                
                // 用户服务路由
                .route("forum-user", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-user")
                )
                
                // 分类服务路由
                .route("forum-category", r -> r
                        .path("/api/v1/categories/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-category")
                )
                
                // 帖子服务路由
                .route("forum-post", r -> r
                        .path("/api/v1/posts/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-post")
                )
                
                // 评论服务路由
                .route("forum-comment", r -> r
                        .path("/api/v1/comments/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-comment")
                )
                
                // 交互服务路由
                .route("forum-interaction", r -> r
                        .path("/api/v1/interactions/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-interaction")
                )
                
                // 举报服务路由
                .route("forum-report", r -> r
                        .path("/api/v1/reports/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-report")
                )
                
                // 统计服务路由
                .route("forum-stats", r -> r
                        .path("/api/v1/stats/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-stats")
                )
                
                // 通知服务路由
                .route("forum-notify", r -> r
                        .path("/api/v1/notices/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-notify")
                )
                
                // 文件服务路由
                .route("forum-file", r -> r
                        .path("/api/v1/files/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Name", "forum-gateway"))
                        .uri("lb://forum-file")
                )
                
                .build();
    }

}
