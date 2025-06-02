package com.fpt.metroll.shared.config;

import com.fpt.metroll.shared.util.converter.MultiSortMapConverter;
import com.fpt.metroll.shared.util.converter.SingleSortMapConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MultiSortMapConverter multiSortMapConverter;
    private final SingleSortMapConverter singleSortMapConverter;

    public WebConfig(MultiSortMapConverter multiSortMapConverter, SingleSortMapConverter singleSortMapConverter) {
        this.multiSortMapConverter = multiSortMapConverter;
        this.singleSortMapConverter = singleSortMapConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(singleSortMapConverter);
        registry.addConverter(multiSortMapConverter);
    }

}