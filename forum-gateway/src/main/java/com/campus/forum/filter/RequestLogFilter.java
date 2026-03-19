package com.campus.forum.filter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 * 
 * <p>记录所有经过网关的请求日志，便于调试和问题排查。</p>
 * 
 * <p>记录内容包括：</p>
 * <ul>
 *     <li>请求ID：唯一标识一次请求</li>
 *     <li>请求路径：请求的URI路径</li>
 *     <li>请求方法：GET、POST、PUT、DELETE等</li>
 *     <li>请求来源：客户端IP地址</li>
 *     <li>请求时间：请求开始时间</li>
 *     <li>响应时间：请求处理耗时</li>
 *     <li>请求状态：成功或失败</li>
 * </ul>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    /**
     * 请求ID属性键
     */
    private static final String REQUEST_ID_ATTR = "X-Request-Id";
    
    /**
     * 请求开始时间属性键
     */
    private static final String REQUEST_START_TIME_ATTR = "X-Request-Start-Time";

    /**
     * 过滤器执行方法
     * 
     * @param exchange ServerWebExchange对象
     * @param chain GatewayFilterChain过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 生成请求ID
        String requestId = IdUtil.fastSimpleUUID();
        
        // 获取请求信息
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        String clientIp = getClientIp(request);
        String startTime = DateUtil.now();
        
        // 存储请求开始时间
        long startTimestamp = System.currentTimeMillis();
        exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);
        exchange.getAttributes().put(REQUEST_START_TIME_ATTR, startTimestamp);
        
        // 记录请求开始日志
        log.info(">>> 请求开始 [{}] {} {} 来自: {} 时间: {}", 
                requestId, method, path, clientIp, startTime);
        
        // 在请求头中添加请求ID
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_ATTR, requestId)
                .build();
        
        // 继续执行过滤器链
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    // 请求完成后记录日志
                    long endTimestamp = System.currentTimeMillis();
                    Long startTimeAttr = exchange.getAttribute(REQUEST_START_TIME_ATTR);
                    long duration = startTimeAttr != null ? endTimestamp - startTimeAttr : 0;
                    
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                            ? exchange.getResponse().getStatusCode().value() 
                            : 0;
                    
                    log.info("<<< 请求结束 [{}] {} {} 状态: {} 耗时: {}ms",
                            requestId, method, path, statusCode, duration);
                }));
    }

    /**
     * 获取客户端真实IP地址
     * 
     * <p>支持获取通过代理或负载均衡后的真实IP地址。</p>
     * 
     * @param request ServerHttpRequest对象
     * @return 客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        // 尝试从X-Forwarded-For获取
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }
        
        // 尝试从X-Real-IP获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        // 尝试从Proxy-Client-IP获取
        String proxyClientIp = request.getHeaders().getFirst("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        // 尝试从WL-Proxy-Client-IP获取
        String wlProxyClientIp = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        // 从RemoteAddress获取
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }

    /**
     * 过滤器执行顺序
     * 
     * <p>请求日志过滤器应该在其他过滤器之前执行，以便记录完整的请求过程。</p>
     * 
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return -200;
    }

}
