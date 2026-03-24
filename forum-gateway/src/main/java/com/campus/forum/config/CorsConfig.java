package com.campus.forum.config;

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

/**
 * 跨域配置类
 * 
 * <p>配置Spring Cloud Gateway的跨域访问策略，允许前端应用跨域访问API。</p>
 * 
 * <p>配置内容包括：</p>
 * <ul>
 *     <li>允许的请求来源</li>
 *     <li>允许的请求方法</li>
 *     <li>允许的请求头</li>
 *     <li>是否允许携带凭证</li>
 *     <li>预检请求缓存时间</li>
 * </ul>
 * 
 * <h3>安全警告</h3>
 * <p><b>生产环境必须配置具体的允许域名！</b></p>
 * <p>当前配置允许所有来源跨域访问并携带凭证，这存在以下安全风险：</p>
 * <ul>
 *     <li>CSRF攻击风险：恶意网站可以伪造用户请求</li>
 *     <li>数据泄露风险：恶意网站可以获取敏感数据</li>
 *     <li>会话劫持风险：恶意网站可能窃取用户会话</li>
 * </ul>
 * 
 * <h3>生产环境配置示例</h3>
 * <p>在application.yml中配置：</p>
 * <pre>
 * cors:
 *   allowed-origins:
 *     - https://example.com
 *     - https://admin.example.com
 *     - https://app.example.com
 * </pre>
 * 
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class CorsConfig {

    private final Environment environment;

    /**
     * 允许的跨域来源域名列表
     * 
     * <p>生产环境必须在application.yml中配置具体的域名，例如：</p>
     * <pre>
     * cors:
     *   allowed-origins: https://example.com,https://admin.example.com
     * </pre>
     * 
     * <p><b>【安全修复】如果不配置，将使用安全的默认值：</b></p>
     * <ul>
     *     <li>开发环境(dev/local)：允许 localhost 来源</li>
     *     <li>生产环境：拒绝所有跨域请求</li>
     * </ul>
     */
    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 应用启动时的配置检查
     * 
     * 【安全修复】检查 CORS 配置是否安全
     */
    @PostConstruct
    public void init() {
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            String[] activeProfiles = environment.getActiveProfiles();
            boolean isDevOrLocal = Arrays.stream(activeProfiles)
                    .anyMatch(p -> "dev".equals(p) || "local".equals(p) || "development".equals(p));
            
            log.warn("=" .repeat(60));
            log.warn("【安全警告】未配置 CORS 允许的来源(cors.allowed-origins)！");
            if (isDevOrLocal) {
                log.warn("当前为开发环境，将允许 localhost 来源访问。");
            } else {
                log.warn("当前为生产环境，将拒绝所有跨域请求！");
                log.warn("请在配置文件中设置允许的域名，例如：");
                log.warn("  cors.allowed-origins=https://example.com,https://admin.example.com");
            }
            log.warn("=".repeat(60));
        } else {
            log.info("CORS 允许来源已配置: {}", allowedOrigins);
        }
    }

    /**
     * 创建跨域过滤器
     * 
     * <p>配置全局跨域规则，应用于所有路由。</p>
     * 
     * @return CorsWebFilter 跨域过滤器
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        // 创建CorsConfiguration对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // 配置允许的来源
        configureAllowedOrigins(corsConfiguration);
        
        // 允许的请求方法
        corsConfiguration.addAllowedMethod("GET");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedMethod("PUT");
        corsConfiguration.addAllowedMethod("DELETE");
        corsConfiguration.addAllowedMethod("OPTIONS");
        corsConfiguration.addAllowedMethod("PATCH");
        corsConfiguration.addAllowedMethod("HEAD");
        
        // 允许的请求头
        corsConfiguration.addAllowedHeader("*");
        
        // 允许携带凭证（如Cookie、Authorization头等）
        // 注意：当allowCredentials为true时，allowedOrigins不能使用"*"
        corsConfiguration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        // 在此时间内，相同的跨域请求不再发送预检请求
        corsConfiguration.setMaxAge(3600L);
        
        // 暴露的响应头（前端可以获取的响应头）
        corsConfiguration.addExposedHeader("Authorization");
        corsConfiguration.addExposedHeader("Content-Disposition");
        corsConfiguration.addExposedHeader("X-Request-Id");
        
        // 创建URL配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 对所有路径应用跨域配置
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsWebFilter(source);
    }

    /**
     * 配置允许的跨域来源
     * 
     * <p>【安全修复】根据配置文件中的设置来配置允许的域名：</p>
     * <ul>
     *     <li>如果配置了具体的域名列表，则只允许这些域名跨域访问</li>
     *     <li>如果没有配置且为开发环境，则允许 localhost 来源</li>
     *     <li>如果没有配置且为生产环境，则不添加任何来源（拒绝所有跨域请求）</li>
     * </ul>
     * 
     * @param corsConfiguration CORS配置对象
     */
    private void configureAllowedOrigins(CorsConfiguration corsConfiguration) {
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            // 已配置：使用配置的具体域名列表
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            for (String origin : origins) {
                String trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    corsConfiguration.addAllowedOrigin(trimmedOrigin);
                    log.debug("CORS 添加允许来源: {}", trimmedOrigin);
                }
            }
        } else {
            // 未配置：根据环境使用安全的默认值
            String[] activeProfiles = environment.getActiveProfiles();
            boolean isDevOrLocal = Arrays.stream(activeProfiles)
                    .anyMatch(p -> "dev".equals(p) || "local".equals(p) || "development".equals(p));
            
            if (isDevOrLocal) {
                // 开发环境：仅允许 localhost 来源
                corsConfiguration.addAllowedOrigin("http://localhost:*");
                corsConfiguration.addAllowedOrigin("http://127.0.0.1:*");
                log.info("CORS 开发模式：允许 localhost 来源");
            } else {
                // 生产环境：不添加任何来源，拒绝所有跨域请求
                // 这样 CORS 策略会拒绝所有跨域请求，除非前端和后端同源
                log.error("CORS 生产模式：未配置允许来源，将拒绝所有跨域请求！");
            }
        }
    }

}
