package com.example.stormgate_product_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .version("1.0.0")
                        .description("Product Management Microservice for the Stormgate E-commerce Platform")
                        .contact(new Contact()
                                .name("API Support")
                                .url("https://example.com")
                                .email("support@example.com")));
    }
}
