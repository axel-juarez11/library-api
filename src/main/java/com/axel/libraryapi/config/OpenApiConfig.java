package com.axel.libraryapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI libraryOpenApi(){
        return new OpenAPI().info(new Info()
                .title("Library API")
                .description("API REST para gestionar libros, socios, préstamos y multas de una biblioteca")
                .version("1.0.0"));
    }
}
