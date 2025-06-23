package com.fpt.metroll.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
@OpenAPIDefinition(
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "https://metroll.southeastasia.cloudapp.azure.com", description = "Production Server"),
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "Development Server")
        }
)
@Configuration
public class OpenAPIConfig {
}
