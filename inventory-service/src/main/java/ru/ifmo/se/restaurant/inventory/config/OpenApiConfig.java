package ru.ifmo.se.restaurant.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:49999");
        gatewayServer.setDescription("API Gateway");

        Info info = new Info()
                .title("Inventory Service API")
                .version("1.0.0")
                .description("API для управления запасами ресторана. " +
                        "Сервис отвечает за управление ингредиентами, инвентарём, поставщиками и заказами на поставку.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(gatewayServer));
    }
}
