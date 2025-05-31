package com.fpt.metroll.shared.util.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * Request interceptor that logs details about incoming HTTP requests and
 * outgoing responses.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    private final RequestLoggerInterface requestLogger;

    // ThreadLocal to store request start time and request ID
    private final ThreadLocal<RequestContext> requestContext = ThreadLocal.withInitial(RequestContext::new);

    @Autowired
    public RequestLoggingInterceptor(RequestLoggerInterface requestLogger) {
        this.requestLogger = requestLogger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate a unique request ID
        String requestId = UUID.randomUUID().toString();

        // Store the start time and request ID
        requestContext.get().startTime = System.currentTimeMillis();
        requestContext.get().requestId = requestId;

        // Extract the user ID from the security context if available
        String userId = extractUserId(request);

        // Log the request
        requestLogger.logRequest(
                request.getRequestURI(),
                request.getMethod(),
                requestId,
                getRequestBody(request),
                userId);

        return true; // Continue processing the request
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        // No operation needed here
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        RequestContext context = requestContext.get();
        long executionTime = System.currentTimeMillis() - context.startTime;

        if (ex != null) {
            // An exception occurred, log it
            requestLogger.logException(
                    request.getRequestURI(),
                    context.requestId,
                    ex,
                    extractUserId(request));
        } else {
            // No exception, log the response
            requestLogger.logResponse(
                    context.requestId,
                    response.getStatus(),
                    getResponseBody(response),
                    executionTime);
        }

        // Clean up the ThreadLocal
        requestContext.remove();
    }

    /**
     * Extract the user ID from the security context if available
     */
    private String extractUserId(HttpServletRequest request) {
        // In a real implementation, this would extract the user ID from security
        // context
        // For example, using Spring Security's SecurityContextHolder
        return null; // Placeholder
    }

    /**
     * Get the request body (in a real implementation, this would need to use a
     * request wrapper
     * that caches the input stream, as the input stream can only be read once)
     */
    private String getRequestBody(HttpServletRequest request) {
        // Placeholder - in a real implementation this would extract and cache the
        // request body
        return "[Request body not captured]";
    }

    /**
     * Get the response body (in a real implementation, this would need to use a
     * response wrapper
     * that caches the output stream)
     */
    private String getResponseBody(HttpServletResponse response) {
        // Placeholder - in a real implementation this would extract and cache the
        // response body
        return "[Response body not captured]";
    }

    /**
     * Helper class to store request context data in ThreadLocal
     */
    private static class RequestContext {
        long startTime;
        String requestId;
    }
}