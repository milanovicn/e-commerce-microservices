package com.example.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI notificationServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("RESTful API for sending and managing notifications")
                        .version("1.0.0"));
    }
}
