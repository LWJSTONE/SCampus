package com.campus.forum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 配置OpenAPI文档
 *
 * @author campus
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer";

    /**
     * 配置OpenAPI
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                // 添加全局认证
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .schemaRequirement(SECURITY_SCHEME_NAME, securityScheme());
    }

    /**
     * API基本信息
     *
     * @return Info
     */
    private Info apiInfo() {
        return new Info()
                .title("校园论坛API文档")
                .description("校园论坛系统RESTful API接口文档")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Campus Team")
                        .email("campus@example.com")
                        .url("https://campus.example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    /**
     * 安全认证配置
     *
     * @return SecurityScheme
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("请输入JWT令牌");
    }
}
