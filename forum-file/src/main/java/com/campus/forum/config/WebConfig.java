package com.campus.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * @author campus
 * @since 2024-01-01
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.storage.local-path:/data/files}")
    private String localStoragePath;

    /**
     * 配置静态资源映射
     * 将本地存储路径映射为可访问的URL
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将/files/**映射到本地文件存储目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + localStoragePath + "/");
    }
}
