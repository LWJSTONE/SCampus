package com.campus.forum.filter;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局认证过滤器
 * 
 * <p>实现JWT Token验证，对所有进入网关的请求进行身份认证。</p>
 * 
 * <p>功能说明：</p>
 * <ul>
 *     <li>白名单路径放行：登录、注册、验证码等公开接口不需要认证</li>
 *     <li>JWT Token验证：验证Token的有效性、过期时间等</li>
 *     <li>用户信息传递：将解析出的用户信息添加到请求头传递给下游服务</li>
 *     <li>统一错误响应：认证失败时返回统一的错误信息</li>
 * </ul>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * JWT签名密钥（生产环境应从配置中心获取）
     */
    @Value("${jwt.secret:campus-forum-jwt-secret-key-2024}")
    private String jwtSecret;

    /**
     * JWT Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 路径匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径列表 - 不需要认证的路径
     * 
     * <p>包含：</p>
     * <ul>
     *     <li>登录相关接口</li>
     *     <li>注册相关接口</li>
     *     <li>验证码接口</li>
     *     <li>密码找回接口</li>
     *     <li>公开信息接口</li>
     *     <li>健康检查接口</li>
     *     <li>Swagger文档接口</li>
     * </ul>
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 认证相关 - 白名单
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/logout",
            "/api/v1/auth/captcha/**",
            "/api/v1/auth/code/**",
            "/api/v1/auth/email/code",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/check-username",
            "/api/v1/auth/check-email",
            "/api/v1/auth/password/reset",
            
            // 公开信息
            "/api/v1/categories/**",
            "/api/v1/posts/list",
            "/api/v1/posts/detail/**",
            "/api/v1/posts/hot",
            "/api/v1/posts/search",
            "/api/v1/users/public/**",
            "/api/v1/stats/public/**",
            
            // 健康检查
            "/actuator/**",
            "/health",
            
            // Swagger文档
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/doc.html",
            "/swagger-resources/**",
            
            // 静态资源
            "/favicon.ico",
            "/static/**",
            "/error"
    );

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
        String path = request.getURI().getPath();
        
        log.debug("Gateway处理请求: {}", path);
        
        // 检查是否为白名单路径
        if (isWhitePath(path)) {
            log.debug("白名单路径，放行: {}", path);
            return chain.filter(exchange);
        }
        
        // 获取Authorization头
        String authorization = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        
        // 检查Token是否存在
        if (!StringUtils.hasText(authorization)) {
            log.warn("请求未携带Token: {}", path);
            return unauthorizedResponse(exchange, "未登录，请先登录");
        }
        
        // 检查Token格式
        if (!authorization.startsWith(TOKEN_PREFIX)) {
            log.warn("Token格式错误: {}", path);
            return unauthorizedResponse(exchange, "Token格式错误");
        }
        
        // 提取Token
        String token = authorization.substring(TOKEN_PREFIX.length());
        
        try {
            // 验证Token
            DecodedJWT jwt = verifyToken(token);
            
            // 提取用户信息
            // 注意：userId在生成Token时是Long类型，需要使用asLong()获取
            Long userIdLong = jwt.getClaim("userId").asLong();
            String userId = String.valueOf(userIdLong);
            String username = jwt.getClaim("username").asString();
            // 获取角色信息，如果不存在则默认为USER
            String role = jwt.getClaim("role").asString();
            if (role == null || role.isEmpty()) {
                role = "USER";
            }
            
            log.debug("Token验证成功, userId: {}, username: {}", userId, username);
            
            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}, 错误: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "Token无效或已过期: " + e.getMessage());
        } catch (Exception e) {
            log.error("认证过程发生异常: {}", path, e);
            return unauthorizedResponse(exchange, "认证服务异常");
        }
    }

    /**
     * 检查路径是否在白名单中
     * 
     * @param path 请求路径
     * @return true-白名单路径，false-需要认证
     */
    private boolean isWhitePath(String path) {
        for (String whitePath : WHITE_LIST) {
            if (pathMatcher.match(whitePath, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证JWT Token
     * 
     * @param token JWT Token字符串
     * @return DecodedJWT 解码后的JWT对象
     * @throws JWTVerificationException Token验证失败异常
     */
    private DecodedJWT verifyToken(String token) throws JWTVerificationException {
        // 创建验证器
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("campus-forum")
                .build();
        
        // 验证Token
        return verifier.verify(token);
    }

    /**
     * 返回未授权响应
     * 
     * @param exchange ServerWebExchange对象
     * @param message 错误消息
     * @return Mono<Void>
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置响应状态码
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        
        // 设置响应头
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        
        // 构建响应体
        Map<String, Object> result = new HashMap<>(4);
        result.put("code", HttpStatus.UNAUTHORIZED.value());
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        
        // 写入响应
        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器执行顺序
     * 
     * <p>值越小优先级越高，认证过滤器应该优先执行。</p>
     * 
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return -100;
    }

}
