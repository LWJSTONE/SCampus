package com.campus.forum.config;

import com.campus.forum.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置跨域、拦截器、静态资源等
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置跨域
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                // 允许的请求来源
                .allowedOriginPatterns("*")
                // 是否允许携带Cookie
                .allowCredentials(true)
                // 预检请求的有效期，单位为秒
                .maxAge(3600);
        
        log.info("跨域配置完成");
    }

    /**
     * 配置静态资源
     *
     * @param registry 资源处理注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Knife4j文档资源
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        // 上传文件资源
        registry.addResourceHandler(Constants.UPLOAD_PATH + "**")
                .addResourceLocations("file:" + Constants.UPLOAD_DIR);
        
        log.info("静态资源配置完成");
    }

    /**
     * 配置拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 可以在这里添加拦截器
        // registry.addInterceptor(new AuthInterceptor())
        //         .addPathPatterns("/**")
        //         .excludePathPatterns("/auth/**", "/doc.html", "/webjars/**", "/swagger-resources/**");
        
        log.info("拦截器配置完成");
    }
}
