package com.campus.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
 * @author Campus Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class CorsConfig {

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
        
        // 允许所有来源跨域访问（生产环境建议配置具体域名）
        corsConfiguration.addAllowedOriginPattern("*");
        
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

}
