package ru.ifmo.se.restaurant.menu.config;

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
    public OpenAPI menuServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8083");
        devServer.setDescription("Development Server (Direct)");

        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:49999/menu");
        prodServer.setDescription("Production Server (via API Gateway)");

        Contact contact = new Contact();
        contact.setName("Menu Service Team");
        contact.setEmail("menu@restaurant.ifmo.ru");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Menu Service API")
                .version("1.0.0")
                .contact(contact)
                .description("API для управления меню ресторана. " +
                        "Сервис отвечает за управление категориями, блюдами и ингредиентами меню.")
                .termsOfService("https://restaurant.ifmo.ru/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
