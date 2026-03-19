package com.campus.forum.config;

import com.campus.forum.api.comment.CommentApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置类
 * 
 * 配置OpenFeign客户端扫描路径
 * 
 * @author campus
 * @since 2024-01-01
 */
@Configuration
@EnableFeignClients(basePackageClasses = {
        CommentApi.class
})
public class FeignConfig {
    // Feign客户端配置
}
