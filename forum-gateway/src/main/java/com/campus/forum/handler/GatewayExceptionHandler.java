package com.campus.forum.handler;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器
 * 
 * <p>统一处理网关层面的异常，返回标准化的错误响应。</p>
 * 
 * <p>处理的异常类型：</p>
 * <ul>
 *     <li>ResponseStatusException：Spring WebFlux响应状态异常</li>
 *     <li>ServiceUnavailableException：服务不可用异常</li>
 *     <li>ConnectException：连接异常（服务未启动等）</li>
 *     <li>TimeoutException：超时异常</li>
 *     <li>Exception：其他未知异常</li>
 * </ul>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * 允许的CORS域名列表
     * 【安全修复】用于动态设置 Access-Control-Allow-Origin，避免与 allowCredentials: true 冲突
     */
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "http://localhost:8081",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:8081"
            // 生产环境需要添加实际的域名
    );

    /**
     * 异常处理方法
     * 
     * @param exchange ServerWebExchange对象
     * @param ex 异常对象
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 如果响应已经提交，直接返回
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        
        // 设置响应头
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        // 【安全修复】动态设置CORS头，避免与 allowCredentials: true 冲突
        String origin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (origin != null && isAllowedOrigin(origin)) {
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, X-Requested-With");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        
        // 构建错误响应
        Map<String, Object> result = new HashMap<>(4);
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", null);
        
        // 根据异常类型设置不同的状态码和消息
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            response.setStatusCode(responseStatusException.getStatusCode());
            result.put("code", responseStatusException.getStatusCode().value());
            result.put("message", responseStatusException.getReason());
            log.warn("网关响应异常: {} - {}", responseStatusException.getStatusCode(), responseStatusException.getReason());
        } else {
            // 其他异常默认返回500
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            result.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.put("message", getErrorMessage(ex));
            log.error("网关内部异常: {}", ex.getMessage(), ex);
        }
        
        // 写入响应
        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 获取异常错误消息
     * 
     * <p>【安全修复】不返回敏感的技术细节信息，只返回用户友好的错误消息。</p>
     * <p>敏感信息如堆栈信息、数据库连接信息、文件路径等不应暴露给客户端。</p>
     * 
     * @param ex 异常对象
     * @return 错误消息
     */
    private String getErrorMessage(Throwable ex) {
        String message = ex.getMessage();
        
        if (message == null || message.isEmpty()) {
            return "服务器内部错误";
        }
        
        // 处理常见异常消息，返回用户友好的错误信息，不暴露技术细节
        if (message.contains("Connection refused") || 
            message.contains("connection") || 
            message.contains("Connection")) {
            return "服务暂时不可用，请稍后重试";
        }
        
        if (message.contains("Read timed out") || 
            message.contains("Timeout") || 
            message.contains("timeout")) {
            return "请求超时，请稍后重试";
        }
        
        if (message.contains("Service Unavailable") || 
            message.contains("Unavailable")) {
            return "服务暂时不可用，请稍后重试";
        }
        
        if (message.contains("Not Found") || 
            message.contains("not found")) {
            return "请求的资源不存在";
        }
        
        if (message.contains("Too Many Requests")) {
            return "请求过于频繁，请稍后重试";
        }
        
        if (message.contains("Unauthorized") || 
            message.contains("Forbidden")) {
            return "访问权限不足";
        }
        
        // 【安全修复】不返回具体的异常消息，防止泄露敏感技术信息
        // 如数据库连接字符串、文件路径、类名等
        return "服务器内部错误";
    }

    /**
     * 检查来源是否在允许列表中
     * 
     * @param origin 请求来源
     * @return true-允许，false-不允许
     */
    private boolean isAllowedOrigin(String origin) {
        return ALLOWED_ORIGINS.contains(origin);
    }

}
