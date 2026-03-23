package com.campus.forum.config;

import com.campus.forum.constant.Constants;
import com.campus.forum.utils.JwtUtils;
import com.campus.forum.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全配置类
 * 
 * <p>配置认证服务相关的安全设置，包括：</p>
 * <ul>
 *   <li>CORS跨域配置</li>
 *   <li>拦截器配置</li>
 *   <li>白名单路径配置</li>
 * </ul>
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final RedisUtils redisUtils;
    
    /**
     * JWT密钥 - 从配置文件读取，用于验证Token
     */
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 白名单路径（不需要Token验证的路径）
     * 注意：路径需要与Controller中的@RequestMapping完整匹配
     */
    private static final String[] WHITE_LIST = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/auth/captcha",
            "/api/v1/auth/password/reset",
            "/api/v1/auth/health",
            "/api/v1/auth/email/code",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/druid/**"
    };

    /**
     * 配置CORS跨域
     * 注意：生产环境应限制允许的域名
     *
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的域名（生产环境应配置具体域名）
        // 开发环境允许localhost，生产环境应配置实际域名
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        // 生产环境请取消注释并配置实际域名：
        // config.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
        // 允许的请求头
        config.addAllowedHeader("*");
        // 允许的请求方法
        config.addAllowedMethod("*");
        // 允许携带Cookie（注意：与addAllowedOriginPattern组合使用需谨慎）
        config.setAllowCredentials(true);
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        // 暴露的响应头
        config.addExposedHeader(Constants.TOKEN_HEADER);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 配置拦截器
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }

    /**
     * 认证拦截器
     * 用于验证请求中的Token
     */
    @RequiredArgsConstructor
    private class AuthenticationInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // OPTIONS请求直接放行
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }

            // 从请求头获取Token
            String token = getTokenFromRequest(request);
            if (StrUtil.isBlank(token)) {
                writeErrorResponse(response, 401, "未登录，请先登录");
                return false;
            }

            // 验证Token（使用配置的密钥）
            if (!JwtUtils.verifyToken(token, jwtSecret)) {
                writeErrorResponse(response, 401, "Token无效或已过期");
                return false;
            }

            // 检查Token是否在黑名单中
            String blacklistKey = Constants.CACHE_PREFIX + "token:blacklist:" + token;
            if (redisUtils.hasKey(blacklistKey)) {
                writeErrorResponse(response, 401, "Token已失效，请重新登录");
                return false;
            }

            // 获取用户ID并存入请求属性
            Long userId = JwtUtils.getUserId(token);
            if (userId != null) {
                request.setAttribute("userId", userId);
                request.setAttribute("username", JwtUtils.getUsername(token));
            }

            return true;
        }

        /**
         * 从请求头获取Token
         */
        private String getTokenFromRequest(HttpServletRequest request) {
            String authorization = request.getHeader(Constants.TOKEN_HEADER);
            if (StrUtil.isBlank(authorization)) {
                return null;
            }

            if (authorization.startsWith(Constants.TOKEN_PREFIX_BEARER)) {
                return authorization.substring(Constants.TOKEN_PREFIX_BEARER.length());
            }

            return authorization;
        }

        /**
         * 写入错误响应
         */
        private void writeErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(code);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", code);
            result.put("message", message);
            result.put("data", null);
            result.put("timestamp", System.currentTimeMillis());
            
            response.getWriter().write(objectMapper.writeValueAsString(result));
        }
    }
}
