package com.taegun.fantasy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * React와 Spring Boot가 서로 통신할 수 있도록
 * CORS 요청을 허용합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")

                // 개발 중 localhost의 Vite 포트를 허용합니다.
                .allowedOriginPatterns("http://localhost:*")

                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )

                .allowedHeaders("*");
    }
}