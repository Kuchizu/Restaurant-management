package ru.ifmo.se.restaurant.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:49999");
        gatewayServer.setDescription("API Gateway");

        Info info = new Info()
                .title("Order Service API")
                .version("1.0.0")
                .description("API для управления заказами ресторана. " +
                        "Сервис отвечает за создание заказов, управление позициями в заказе, " +
                        "отправку заказов на кухню и закрытие заказов.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(gatewayServer));
    }
}
