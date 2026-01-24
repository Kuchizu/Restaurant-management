package ru.ifmo.se.restaurant.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders(
                        "X-Total-Count",
                        "X-Total-Pages",
                        "X-Page-Number",
                        "X-Page-Size",
                        "X-Has-Next",
                        "X-Has-Previous"
                )
                .maxAge(3600);
    }
}
