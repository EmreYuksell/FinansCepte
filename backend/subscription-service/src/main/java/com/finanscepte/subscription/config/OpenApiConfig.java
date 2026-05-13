package com.finanscepte.subscription.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI subscriptionServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CepteFinans Subscription Service API")
                        .description("Subscription management service")
                        .version("1.0"));
    }
}
