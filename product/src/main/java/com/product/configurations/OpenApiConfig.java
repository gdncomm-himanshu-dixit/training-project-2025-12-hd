package com.product.configurations;

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
                        .title("Product Service")
                        .version("1.0")
                        .description("API documentation for CRUD application for Product Service which will be responsible for Product related Operations ")
                        .contact(new Contact()
                                .name("")
                                .email("")));
    }
}