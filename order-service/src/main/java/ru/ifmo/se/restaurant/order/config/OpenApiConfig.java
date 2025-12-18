package ru.ifmo.se.restaurant.order.config;

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
    public OpenAPI orderServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8081");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:49999/order");
        prodServer.setDescription("Production Server (via API Gateway)");

        Contact contact = new Contact();
        contact.setName("Order Service Team");
        contact.setEmail("order@restaurant.ifmo.ru");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Order Service API")
                .version("1.0.0")
                .contact(contact)
                .description("API для управления заказами ресторана. " +
                        "Сервис отвечает за создание заказов, управление позициями в заказе, " +
                        "отправку заказов на кухню и закрытие заказов.")
                .termsOfService("https://restaurant.ifmo.ru/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
