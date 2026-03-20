package com.campus.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Forum Gateway Application
 * 
 * <p>API Gateway Service provides:</p>
 * <ul>
 *     <li>Route forwarding to microservices</li>
 *     <li>JWT authentication</li>
 *     <li>CORS handling</li>
 *     <li>Request logging</li>
 * </ul>
 * 
 * @author Campus Team
 * @version 1.0.0
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(
    basePackages = {"com.campus.forum"},
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.campus\\.forum\\.exception\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.campus\\.forum\\.config\\.WebMvcConfig"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.campus\\.forum\\.config\\.MybatisPlusConfig"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.campus\\.forum\\.config\\.RedisConfig"
        )
    }
)
public class ForumGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumGatewayApplication.class, args);
        System.out.println("==========================================");
        System.out.println("    Forum Gateway Started Successfully    ");
        System.out.println("    Gateway Port: 8080                   ");
        System.out.println("==========================================");
    }

}
