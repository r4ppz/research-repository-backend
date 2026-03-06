package com.acd.researchrepo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  // To use it:
  // > http://localhost:8080/swagger-ui/index.html
  // Add proper API documentation later

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Research Repository API")
                .version("1.0")
                .description("Research Repository System API Documentation"));
  }
}
