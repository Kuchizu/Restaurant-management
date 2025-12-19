package ru.ifmo.se.restaurant.kitchen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

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
