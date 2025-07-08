package com.fpt.metroll.order.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
@ConfigurationProperties(prefix = "metroll.payos")
@Data
@Slf4j
public class PayOSConfig {
    
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String webhookUrl;
    
    @Bean
    public PayOS payOS() {
        if (clientId.isEmpty() || apiKey.isEmpty() || checksumKey.isEmpty()) {
            log.warn("PayOS credentials not configured. Payment processing will be mocked.");
            throw  new RuntimeException("PayOS credentials not configured.");
        }
        log.info(
            "Configuring PayOS with client ID: {}, API Key: {}, Checksum Key: {}",
            clientId, apiKey, checksumKey);
        log.info("Initializing PayOS with client ID: {}", clientId);
        return new PayOS(clientId, apiKey, checksumKey);
    }
} 