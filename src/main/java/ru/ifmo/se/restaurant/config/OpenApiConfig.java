package ru.ifmo.se.restaurant.config;

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
                .title("Restaurant Management System API")
                .version("1.1.0")
                .description("API for managing restaurant operations including menu, orders, kitchen, inventory, suppliers, and reporting")
                .contact(new Contact()
                    .name("GitHub Repository")
                    .url("https://github.com/Kuchizu/Restaurant-management")
                ));
    }
}

