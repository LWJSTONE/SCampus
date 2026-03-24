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

import javax.annotation.PostConstruct;
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
     * JWT签名密钥
     * 注意：生产环境必须从环境变量配置，不提供默认值
     */
    @Value("${jwt.secret}")
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
     * 内部服务请求头名称
     * 用于验证内部服务之间的调用，防止外部直接访问内部接口
     */
    private static final String INTERNAL_REQUEST_HEADER = "X-Internal-Request";

    /**
     * 内部服务签名请求头名称
     * 使用签名验证内部服务调用，防止请求伪造
     */
    private static final String INTERNAL_SIGNATURE_HEADER = "X-Internal-Signature";
    
    /**
     * 内部服务密钥 - 从配置中心读取
     * 生产环境必须配置此项
     * 
     * 【安全修复】如果未配置此项，内部服务接口将被拒绝访问
     */
    @Value("${app.internal-service-secret:}")
    private String internalServiceSecret;

    /**
     * 应用启动时的配置检查
     * 
     * 【安全修复】检查内部服务密钥是否已配置
     * 如果未配置，将记录错误日志，并禁用内部服务接口访问
     */
    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(internalServiceSecret)) {
            log.error("=".repeat(60));
            log.error("【安全警告】未配置内部服务密钥(app.internal-service-secret)！");
            log.error("内部服务接口(/api/v1/*/internal/**)将拒绝所有访问请求。");
            log.error("生产环境必须在配置文件中设置此项，例如：");
            log.error("  app.internal-service-secret=your-secure-secret-key");
            log.error("=".repeat(60));
        } else {
            log.info("内部服务密钥已配置，内部服务接口认证已启用。");
        }
    }

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
     * 
     * <p>注意：内部服务调用接口不在白名单中，需要通过{@link #INTERNAL_PATHS}单独处理</p>
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 认证相关 - 白名单
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            // 注意：logout不应该在白名单中，因为需要验证Token才能使Token失效
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
     * 内部服务调用接口路径列表
     * 
     * <p>这些路径需要验证X-Internal-Request请求头，只有带有正确请求头的内部服务调用才被允许。</p>
     * <p>安全说明：外部请求访问这些路径将被拒绝，防止未授权访问内部接口。</p>
     */
    private static final List<String> INTERNAL_PATHS = Arrays.asList(
            "/api/v1/posts/internal/**",
            "/api/v1/users/internal/**",
            "/api/v1/comments/internal/**"
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
        
        // 检查是否为内部服务路径（需要验证内部服务签名）
        if (isInternalPath(path)) {
            String internalHeader = request.getHeaders().getFirst(INTERNAL_REQUEST_HEADER);
            String signature = request.getHeaders().getFirst(INTERNAL_SIGNATURE_HEADER);
            
            // 验证内部服务请求头
            if (!"true".equals(internalHeader)) {
                log.warn("内部服务接口未授权访问(缺少请求头): {}, 来源IP: {}", path, 
                        request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
                return unauthorizedResponse(exchange, "禁止访问内部接口");
            }
            
            // 验证签名（如果配置了密钥）
            if (StringUtils.hasText(internalServiceSecret)) {
                if (!StringUtils.hasText(signature)) {
                    log.warn("内部服务接口缺少签名: {}, 来源IP: {}", path,
                            request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
                    return unauthorizedResponse(exchange, "禁止访问内部接口");
                }
                
                // 验证签名：签名 = HMAC-SHA256(path + timestamp + secret)
                String timestamp = request.getHeaders().getFirst("X-Timestamp");
                if (!StringUtils.hasText(timestamp)) {
                    log.warn("内部服务接口缺少时间戳: {}", path);
                    return unauthorizedResponse(exchange, "禁止访问内部接口");
                }
                
                // 检查时间戳是否在5分钟内有效
                try {
                    long requestTime = Long.parseLong(timestamp);
                    long currentTime = System.currentTimeMillis();
                    if (Math.abs(currentTime - requestTime) > 5 * 60 * 1000) {
                        log.warn("内部服务接口时间戳过期: {}", path);
                        return unauthorizedResponse(exchange, "请求已过期");
                    }
                } catch (NumberFormatException e) {
                    log.warn("内部服务接口时间戳格式错误: {}", path);
                    return unauthorizedResponse(exchange, "禁止访问内部接口");
                }
                
                // 验证签名
                String expectedSignature = generateSignature(path, timestamp, internalServiceSecret);
                if (!expectedSignature.equals(signature)) {
                    log.warn("内部服务接口签名验证失败: {}, 来源IP: {}", path,
                            request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
                    return unauthorizedResponse(exchange, "禁止访问内部接口");
                }
            } else {
                // 【安全修复】未配置密钥时，拒绝访问内部服务接口
                // 不能简单绕过认证，否则攻击者只需设置请求头即可访问内部接口
                log.error("内部服务接口访问被拒绝：未配置内部服务密钥(app.internal-service-secret)，路径: {}, 来源IP: {}", 
                        path, 
                        request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown");
                return unauthorizedResponse(exchange, "内部服务接口未配置安全密钥，访问被拒绝");
            }
            
            log.debug("内部服务接口验证通过: {}", path);
            return chain.filter(exchange);
        }

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
            // 不返回具体错误信息，防止信息泄露
            return unauthorizedResponse(exchange, "Token无效或已过期");
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
     * 检查路径是否为内部服务路径
     * 
     * @param path 请求路径
     * @return true-内部服务路径，false-非内部路径
     */
    private boolean isInternalPath(String path) {
        for (String internalPath : INTERNAL_PATHS) {
            if (pathMatcher.match(internalPath, path)) {
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
     * 生成内部服务签名
     * 使用HMAC-SHA256算法生成签名
     * 
     * @param path 请求路径
     * @param timestamp 时间戳
     * @param secret 密钥
     * @return 签名字符串
     */
    private String generateSignature(String path, String timestamp, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            String data = path + timestamp;
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            return "";
        }
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
