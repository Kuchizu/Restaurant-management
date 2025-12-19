package ru.ifmo.se.restaurant.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.net.URI;

@Controller
public class SwaggerController {

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerUiPath;

    @GetMapping("/")
    public Mono<Void> redirectToSwagger(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create("/webjars/swagger-ui/index.html"));
        return response.setComplete();
    }

    @GetMapping("/swagger-ui")
    public Mono<Void> redirectToSwaggerUi(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create("/webjars/swagger-ui/index.html"));
        return response.setComplete();
    }
}
