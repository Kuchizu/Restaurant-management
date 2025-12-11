package ru.ifmo.se.restaurant.billing.config;

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
    public OpenAPI billingServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8085");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:49999/billing");
        prodServer.setDescription("Production Server (via API Gateway)");

        Contact contact = new Contact();
        contact.setName("Billing Service Team");
        contact.setEmail("billing@restaurant.ifmo.ru");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Billing Service API")
                .version("1.0.0")
                .contact(contact)
                .description("API для управления счетами ресторана. " +
                        "Сервис отвечает за создание счетов, применение скидок, обработку оплаты и отмену счетов.")
                .termsOfService("https://restaurant.ifmo.ru/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
