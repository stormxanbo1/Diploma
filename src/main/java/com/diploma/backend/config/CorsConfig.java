package com.diploma.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/api/**")                  // все ваши REST-эндпоинты
                        .allowedOrigins("http://localhost:3000")// адрес фронтенда
                        .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                        .allowedHeaders("*")                   // можно сузить до перечня своих заголовков
                        .allowCredentials(true)                // если нужны куки/авторизация
                        .maxAge(3600);                         // кэшировать запрос предзапроса 1 час
            }
        };
    }
}
