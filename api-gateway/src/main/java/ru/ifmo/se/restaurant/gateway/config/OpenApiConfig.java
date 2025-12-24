package ru.ifmo.se.restaurant.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:49999");
        server.setDescription("API Gateway Server");

        Contact contact = new Contact();
        contact.setName("Restaurant Management System");
        contact.setEmail("Kuchizu@itmo.ru");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Restaurant Management System - Unified API Documentation")
                .version("1.0.0")
                .contact(contact)
                .description("Единая точка входа для всех микросервисов системы управления рестораном.\n\n" +
                        "**Аутентификация:**\n" +
                        "1. Вызовите POST /api/auth/login с username и password\n" +
                        "2. Скопируйте accessToken из ответа\n" +
                        "3. Нажмите Authorize и введите токен\n\n" +
                        "**Роли:**\n" +
                        "- ADMIN: Полный доступ, управление пользователями\n" +
                        "- MANAGER: Управление меню, инвентарем, персоналом\n" +
                        "- WAITER: Заказы, счета\n" +
                        "- CHEF: Кухня, просмотр инвентаря\n\n" +
                        "**Сервисы:**\n" +
                        "- **Order Service**: Управление заказами, столами, сотрудниками\n" +
                        "- **Kitchen Service**: Очередь кухни и приготовление блюд\n" +
                        "- **Menu Service**: Управление меню, категориями, блюдами\n" +
                        "- **Inventory Service**: Управление складом и поставщиками\n" +
                        "- **Billing Service**: Счета и финансовая отчетность")
                .termsOfService("https://github.com/Kuchizu/Restaurant-management")
                .license(license);

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT токен авторизации. Получите токен через /api/auth/login");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme));
    }
}

