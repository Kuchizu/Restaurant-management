package ru.ifmo.se.restaurant.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
                        "**Сервисы:**\n" +
                        "- **Order Service** (Port 8081): Управление заказами, столами, сотрудниками\n" +
                        "- **Kitchen Service** (Port 8082): Очередь кухни и приготовление блюд\n" +
                        "- **Menu Service** (Port 8083): Управление меню, категориями, блюдами\n" +
                        "- **Inventory Service** (Port 8084): Управление складом и поставщиками\n" +
                        "- **Billing Service** (Port 8085): Счета и финансовая отчетность\n\n" +
                        "Используйте выпадающий список вверху для переключения между сервисами.")
                .termsOfService("https://github.com/Kuchizu/Restaurant-management")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}

