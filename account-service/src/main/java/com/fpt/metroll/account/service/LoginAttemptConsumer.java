package com.fpt.metroll.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class LoginAttemptConsumer {
    @Bean
    public Consumer<Map<String, Object>> loginAttempt() {
        return loginAttempt -> {
            log.info("Received login attempt: userId={}, email={}, timestamp={}, role={}",
                    loginAttempt.get("userId"),
                    loginAttempt.get("email"),
                    loginAttempt.get("timestamp"),
                    loginAttempt.get("role"));
            
            // Add your login attempt processing logic here
            // For example: store in database, send notifications, etc.
        };
    }
} 