package com.campus.forum.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
     * <p>【安全修复】支持反向代理场景，优先从 X-Forwarded-For 或 X-Real-IP 获取真实客户端IP。</p>
     * 
     * @return KeyResolver 限流Key解析器
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = getRealClientIp(exchange.getRequest());
            return Mono.just(ip);
        };
    }

    /**
     * 用户ID限流Key解析器
     * 
     * <p>根据用户ID进行限流，已登录用户按ID限流，未登录用户按IP限流。</p>
     * 
     * <p>【安全修复】支持反向代理场景，优先从 X-Forwarded-For 或 X-Real-IP 获取真实客户端IP。</p>
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
            String ip = getRealClientIp(exchange.getRequest());
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * 获取真实客户端IP地址
     * 
     * <p>【安全修复】在反向代理场景下，直接获取 RemoteAddress 只能得到代理服务器IP，
     * 需要从 X-Forwarded-For 或 X-Real-IP 请求头获取真实客户端IP。</p>
     * 
     * <p>优先级：</p>
     * <ol>
     *     <li>X-Forwarded-For 第一个IP（最原始客户端IP）</li>
     *     <li>X-Real-IP</li>
     *     <li>RemoteAddress（直连场景）</li>
     *     <li>"unknown"（无法获取时）</li>
     * </ol>
     * 
     * @param request ServerHttpRequest 请求对象
     * @return 客户端IP地址
     */
    private String getRealClientIp(ServerHttpRequest request) {
        // 优先从 X-Forwarded-For 获取真实客户端IP
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For 格式: client, proxy1, proxy2
            // 取第一个IP作为真实客户端IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        // 尝试从 X-Real-IP 获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        // 最后使用 RemoteAddress（直连场景）
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
}
