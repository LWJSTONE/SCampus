package com.campus.forum.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel限流处理器
 * 
 * <p>自定义Sentinel网关限流时的响应内容，提供友好的错误提示。</p>
 * 
 * <p>支持以下限流场景：</p>
 * <ul>
 *     <li>API限流：针对特定API路径的限流</li>
 *     <li>参数限流：针对特定参数的限流</li>
 *     <li>IP限流：针对特定IP的限流</li>
 *     <li>用户限流：针对特定用户的限流</li>
 * </ul>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
public class SentinelBlockHandler implements BlockRequestHandler {

    /**
     * 限流响应处理方法
     * 
     * <p>当请求被Sentinel限流时，调用此方法返回限流响应。</p>
     * 
     * @param exchange ServerWebExchange对象
     * @param ex 限流异常
     * @return Mono<ServerResponse> 限流响应
     */
    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        // 获取请求信息
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        
        // 记录限流日志
        log.warn("Sentinel限流触发: {} {} - {}", method, path, ex.getMessage());
        
        // 构建限流响应
        Map<String, Object> result = new HashMap<>(4);
        result.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        result.put("message", "系统繁忙，请求过于频繁，请稍后重试");
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        
        // 添加额外信息（调试模式）
        Map<String, Object> details = new HashMap<>(3);
        details.put("path", path);
        details.put("method", method);
        details.put("reason", "rate_limited");
        result.put("details", details);
        
        // 返回JSON响应
        return ServerResponse
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSON.toJSONString(result));
    }

}
