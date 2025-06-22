package com.fpt.metroll.gateway.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import io.swagger.v3.oas.models.servers.Server;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("production")
public class OpenAPIServerConfig {

    @Bean
    public OpenApiCustomizer serverOpenApiCustomizer() {
        return openApi -> {
            List<Server> servers = new ArrayList<>();
            
            // Production HTTPS server
            Server productionServer = new Server();
            productionServer.setUrl("https://metroll.southeastasia.cloudapp.azure.com");
            productionServer.setDescription("Production Server (HTTPS)");
            servers.add(productionServer);
            
            // Development server for fallback
            Server devServer = new Server();
            devServer.setUrl("http://localhost:8080");
            devServer.setDescription("Development Server");
            servers.add(devServer);
            
            openApi.setServers(servers);
        };
    }
} 