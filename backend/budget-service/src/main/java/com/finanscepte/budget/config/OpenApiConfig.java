package com.finanscepte.budget.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI budgetServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CepteFinans Budget Service API")
                        .description("Budget management service")
                        .version("1.0"));
    }
}
