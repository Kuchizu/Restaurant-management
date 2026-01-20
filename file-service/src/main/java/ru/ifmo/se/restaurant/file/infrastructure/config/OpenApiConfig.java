package ru.ifmo.se.restaurant.file.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fileServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Service API")
                        .description("API for managing file uploads and downloads for the Restaurant Management System")
                        .version("1.0.0"));
    }
}
