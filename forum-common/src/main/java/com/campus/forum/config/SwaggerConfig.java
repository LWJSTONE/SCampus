package com.campus.forum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger OpenAPI Configuration
 *
 * <p>Only enabled for Servlet-based web applications (not for Reactive/Gateway)</p>
 *
 * @author campus
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .schemaRequirement(SECURITY_SCHEME_NAME, securityScheme());
    }

    private Info apiInfo() {
        return new Info()
                .title("Campus Forum API")
                .description("Campus Forum System REST API Documentation")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Campus Team")
                        .email("campus@example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Please enter JWT token");
    }
}
