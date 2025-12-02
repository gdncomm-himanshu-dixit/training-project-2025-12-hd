package com.cart.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cart Service")
                        .version("1.0")
                        .description("API documentation for CRUD application for Cart Service which will be responsible for Cart related Operations ")
                        .contact(new Contact()
                                .name("")
                                .email("")));
    }
}