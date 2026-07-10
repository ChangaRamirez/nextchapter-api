package com.changa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nextChapterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NextChapter API")
                        .description("Production-style REST API for managing personal reading collections, including books, reading progress, reviews, notes and personalized reading statistics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("J. Eduardo \"Changa\" Ramírez-García")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
