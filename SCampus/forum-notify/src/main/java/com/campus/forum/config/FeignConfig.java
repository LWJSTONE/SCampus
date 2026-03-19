package com.campus.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Feign配置类
 * 
 * @author campus
 * @since 2024-01-01
 */
@Configuration
@EnableFeignClients(basePackages = "com.campus.forum.api")
public class FeignConfig {
}
