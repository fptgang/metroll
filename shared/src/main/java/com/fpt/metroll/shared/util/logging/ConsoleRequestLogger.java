package com.fpt.metroll.shared.util.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of RequestLoggerInterface that logs information to the console
 * using SLF4J.
 */
@Component
public class ConsoleRequestLogger implements RequestLoggerInterface {

    private static final Logger log = LoggerFactory.getLogger(ConsoleRequestLogger.class);

    @Override
    public void logRequest(String requestPath, String method, String requestId, String body, String userId) {
        String userInfo = userId != null ? " user=" + userId : " (anonymous)";
        log.info("[{}] {} {} {}{}", requestId.substring(requestId.length()-12), method, requestPath, maskSensitiveData(body), userInfo);
    }

    @Override
    public void logResponse(String requestId, int statusCode, String body, long executionTime) {
        log.info("[{}] Response: status={}, time={}ms, body={}",
                requestId.substring(24), statusCode, executionTime, maskSensitiveData(body));
    }

    @Override
    public void logException(String requestPath, String requestId, Exception exception, String userId) {
        String userInfo = userId != null ? " user=" + userId : " (anonymous)";
        log.error("[{}] Exception on {} {}: {}",
                requestId.substring(24), requestPath, userInfo, exception.getMessage(), exception);
    }

    /**
     * Masks sensitive data such as passwords, tokens, etc.
     * 
     * @param data The data to be masked
     * @return Masked data
     */
    private String maskSensitiveData(String data) {
        if (data == null) {
            return "null";
        }

        // This is a simple implementation that could be expanded
        // to use more sophisticated pattern matching
        String masked = data;

        // Mask passwords
        masked = masked.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"*****\"");
        // Mask tokens
        masked = masked.replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"*****\"");
        // Mask credit card numbers
        masked = masked.replaceAll("\\b(?:\\d[ -]*?){13,16}\\b", "**** **** **** ****");

        return masked;
    }
}