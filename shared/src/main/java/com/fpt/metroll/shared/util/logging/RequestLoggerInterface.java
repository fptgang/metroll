package com.fpt.metroll.shared.util.logging;

/**
 * Interface for logging HTTP requests and responses.
 * Implementations can provide different strategies for logging such as:
 * - Console logging
 * - File logging
 * - Database logging
 * - External service logging
 */
public interface RequestLoggerInterface {

    /**
     * Log an incoming request
     * 
     * @param requestPath The request path
     * @param method      The HTTP method (GET, POST, etc.)
     * @param requestId   A unique identifier for the request
     * @param body        The request body (if available)
     * @param userId      The ID of the authenticated user (if available)
     */
    void logRequest(String requestPath, String method, String requestId, String body, String userId);

    /**
     * Log an outgoing response
     * 
     * @param requestId     A unique identifier for the request
     * @param statusCode    The HTTP status code
     * @param body          The response body (if available)
     * @param executionTime The time taken to process the request in milliseconds
     */
    void logResponse(String requestId, int statusCode, String body, long executionTime);

    /**
     * Log an exception that occurred during request processing
     * 
     * @param requestPath The request path
     * @param requestId   A unique identifier for the request
     * @param exception   The exception that was thrown
     * @param userId      The ID of the authenticated user (if available)
     */
    void logException(String requestPath, String requestId, Exception exception, String userId);
}