package ru.ifmo.se.restaurant.kitchen.config;

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
    public OpenAPI kitchenServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8082");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:49999/kitchen");
        prodServer.setDescription("Production Server (via API Gateway)");

        Contact contact = new Contact();
        contact.setName("Kitchen Service Team");
        contact.setEmail("kitchen@restaurant.ifmo.ru");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Kitchen Service API")
                .version("1.0.0")
                .contact(contact)
                .description("API для управления кухонной очередью ресторана. " +
                        "Сервис отвечает за добавление блюд в очередь, обновление статусов приготовления и отслеживание прогресса заказов.")
                .termsOfService("https://restaurant.ifmo.ru/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
