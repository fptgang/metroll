package com.fpt.metroll.shared.util.http;

import com.fpt.metroll.shared.exception.*;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final HttpHeaders HEADERS = new HttpHeaders();

    static {
        HEADERS.setContentType(MediaType.APPLICATION_JSON);
    }

    @Value("${metroll.exception.log:false}")
    private boolean exceptionLog;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof org.springframework.security.authentication.BadCredentialsException ||
                ex instanceof org.springframework.security.authentication.InsufficientAuthenticationException ||
                ex instanceof org.springframework.security.authentication.AuthenticationCredentialsNotFoundException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            status = ((org.springframework.web.server.ResponseStatusException) ex).getStatusCode();
        } else if (ex instanceof org.springframework.web.bind.MissingServletRequestParameterException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof IllegalArgumentException ||
                ex instanceof IllegalStateException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof NoPermissionException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof PaymentProcessingException) {
            status = HttpStatus.BAD_GATEWAY;
        } else if (ex instanceof FeignException) {
            // Handle Feign exceptions to preserve original status code
            FeignException feignEx = (FeignException) ex;
            status = HttpStatus.valueOf(feignEx.status());
        }

        log.info("Error status {} due to exception {}", status, ex.getClass().getName());

        if (exceptionLog) {
            ex.printStackTrace();
        }

        String errorMessage = ex.getMessage();
        
        // Extract cleaner message from FeignException
        if (ex instanceof FeignException) {
            FeignException feignEx = (FeignException) ex;
            String contentUTF8 = feignEx.contentUTF8();
            if (contentUTF8 != null && !contentUTF8.isEmpty()) {
                // Try to extract error message from JSON response
                if (contentUTF8.contains("\"error\"")) {
                    try {
                        int startIndex = contentUTF8.indexOf("\"error\"");
                        startIndex = contentUTF8.indexOf(":", startIndex) + 1;
                        int endIndex = contentUTF8.indexOf("\"", contentUTF8.indexOf("\"", startIndex) + 1);
                        if (endIndex != -1) {
                            errorMessage = contentUTF8.substring(contentUTF8.indexOf("\"", startIndex) + 1, endIndex);
                        }
                    } catch (Exception e) {
                        // Fall back to original message if JSON parsing fails
                        log.debug("Failed to parse error message from FeignException response", e);
                    }
                }
            }
        }

        ErrorResponse error = ErrorResponse.builder().error(errorMessage).build();
        return ResponseEntity.status(status).body(error);
    }
}
