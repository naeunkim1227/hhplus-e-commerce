package io.hhplus.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .description("E-Commerce 애플리케이션 REST API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@ecommerce.io")));
    }
}