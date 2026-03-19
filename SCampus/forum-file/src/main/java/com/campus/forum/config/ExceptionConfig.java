package com.campus.forum.config;

import com.campus.forum.exception.BusinessException;
import com.campus.forum.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 异常处理配置
 *
 * @author campus
 * @since 2024-01-01
 */
@Configuration
public class ExceptionConfig {

    /**
     * 全局异常处理器
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
