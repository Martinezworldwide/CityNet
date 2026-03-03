package com.citynet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CorsConfig enables cross-origin requests from the GitHub Pages origin so that
// the Java/Spring Boot backend on Render can be called by the static frontend.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow the GitHub Pages origin to call all REST endpoints.
        registry.addMapping("/**")
                .allowedOrigins("https://martinezworldwide.github.io")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}

