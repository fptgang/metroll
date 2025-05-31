package com.fpt.metroll.gateway;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static org.springdoc.core.utils.Constants.DEFAULT_API_DOCS_URL;

@SpringBootApplication(scanBasePackages = {"com.fpt.metroll"})
public class BackendApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.of("+7")));
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    @Lazy(false)
    public Set<SwaggerUrl> apis(RouteDefinitionLocator locator, SwaggerUiConfigProperties swaggerUiConfigProperties) {
        Set<SwaggerUrl> urls = new HashSet<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        definitions.stream().filter(routeDefinition -> routeDefinition.getId().matches(".*-service")).forEach(routeDefinition -> {
            String name = routeDefinition.getId().replaceAll("-service", "");
            SwaggerUrl swaggerUrl = new SwaggerUrl(name, DEFAULT_API_DOCS_URL+"/" + name, null);
            urls.add(swaggerUrl);
        });
        swaggerUiConfigProperties.setUrls(urls);
        return urls;
    }
}

