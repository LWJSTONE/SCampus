package com.campus.forum.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 限流配置类
 * 
 * <p>配置Spring Cloud Gateway的请求限流规则，使用Redis进行分布式限流。</p>
 * 
 * <p>限流策略：</p>
 * <ul>
 *     <li>基于IP地址进行限流</li>
 *     <li>每个IP每秒允许的请求数由各个路由配置决定</li>
 * </ul>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class RateLimiterConfig {

    /**
     * IP地址限流Key解析器
     * 
     * <p>根据客户端IP地址进行限流，同一IP地址的请求共享限流配额。</p>
     * 
     * @return KeyResolver 限流Key解析器
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null 
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * 用户ID限流Key解析器
     * 
     * <p>根据用户ID进行限流，已登录用户按ID限流，未登录用户按IP限流。</p>
     * 
     * @return KeyResolver 限流Key解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }
            // 未登录用户按IP限流
            String ip = exchange.getRequest().getRemoteAddress() != null 
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }
}
