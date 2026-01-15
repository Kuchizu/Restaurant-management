package ru.ifmo.se.restaurant.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Management API")
                        .version("1.0")
                        .description("## Как авторизоваться\n\n" +
                                "1. POST `/api/auth/init` - создать админа (только первый раз)\n" +
                                "2. POST `/api/auth/login` - получить токен\n" +
                                "3. Нажать **Authorize** и ввести токен\n\n" +
                                "**Логин:** admin@restaurant.com\n" +
                                "**Пароль:** admin123"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .packagesToScan("ru.ifmo.se.restaurant.gateway.controller")
                .pathsToMatch("/api/auth/**")
                .build();
    }

}

