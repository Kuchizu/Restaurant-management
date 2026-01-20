package ru.ifmo.se.restaurant.billing.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI billingServiceOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:49999");
        gatewayServer.setDescription("API Gateway");

        Info info = new Info()
                .title("Billing Service API")
                .version("1.0.0")
                .description("API для управления счетами ресторана. " +
                        "Сервис отвечает за создание счетов, применение скидок, обработку оплаты и отмену счетов.");

        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT токен авторизации. Получите токен через /api/auth/login");

        return new OpenAPI()
                .info(info)
                .servers(List.of(gatewayServer))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
