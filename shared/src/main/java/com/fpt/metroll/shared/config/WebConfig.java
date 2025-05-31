package com.fpt.metroll.shared.config;

import com.fpt.metroll.shared.util.converter.MultiSortMapConverter;
import com.fpt.metroll.shared.util.converter.SingleSortMapConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MultiSortMapConverter multiSortMapConverter;
    private final SingleSortMapConverter singleSortMapConverter;

    @Value("${metroll.cors.allow-url:http://localhost:5173}")
    private String FRONTEND_SERVER_URL;

    public WebConfig(MultiSortMapConverter multiSortMapConverter, SingleSortMapConverter singleSortMapConverter) {
        this.multiSortMapConverter = multiSortMapConverter;
        this.singleSortMapConverter = singleSortMapConverter;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedOrigins(FRONTEND_SERVER_URL)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(singleSortMapConverter);
        registry.addConverter(multiSortMapConverter);
    }

}