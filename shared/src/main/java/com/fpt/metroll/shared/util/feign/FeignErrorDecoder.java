package com.fpt.metroll.shared.util.feign;

import feign.codec.ErrorDecoder;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String requestUrl = response.request().url();
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        String message = extractErrorMessage(response);
        
        log.warn("Feign client error for {} {}: {} - {}", 
                response.request().httpMethod(), requestUrl, httpStatus, message);
        
        // For 4xx errors, preserve the original status and message
        if (httpStatus.is4xxClientError()) {
            return new ResponseStatusException(httpStatus, message);
        }
        
        // For 5xx errors, also preserve but log as error
        if (httpStatus.is5xxServerError()) {
            log.error("Feign client server error for {} {}: {} - {}", 
                    response.request().httpMethod(), requestUrl, httpStatus, message);
            return new ResponseStatusException(httpStatus, message);
        }
        
        // Default fallback
        return new ResponseStatusException(httpStatus, message);
    }
    
    private String extractErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                byte[] bodyBytes = response.body().asInputStream().readAllBytes();
                String responseBody = new String(bodyBytes, StandardCharsets.UTF_8);
                
                // Try to extract error message from JSON response
                if (responseBody.contains("\"error\"")) {
                    // Simple JSON parsing to extract error message
                    int startIndex = responseBody.indexOf("\"error\"");
                    if (startIndex != -1) {
                        startIndex = responseBody.indexOf(":", startIndex) + 1;
                        int endIndex = responseBody.indexOf("\"", responseBody.indexOf("\"", startIndex) + 1);
                        if (endIndex != -1) {
                            return responseBody.substring(responseBody.indexOf("\"", startIndex) + 1, endIndex);
                        }
                    }
                }
                
                // If no structured error message, return the full response body (truncated if too long)
                return responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
            }
        } catch (IOException e) {
            log.warn("Failed to read response body", e);
        }
        
        // Fallback to status reason if no body is available
        return response.reason() != null ? response.reason() : "Service unavailable";
    }
} 