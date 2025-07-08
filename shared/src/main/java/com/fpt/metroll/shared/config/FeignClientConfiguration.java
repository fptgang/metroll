package com.fpt.metroll.shared.config;

import com.fpt.metroll.shared.security.FeignClientInterceptor;
import com.fpt.metroll.shared.util.feign.FeignErrorDecoder;
import feign.codec.ErrorDecoder;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new FeignClientInterceptor();
    }
} 