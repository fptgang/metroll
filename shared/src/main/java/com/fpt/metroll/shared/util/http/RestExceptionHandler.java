package com.fpt.metroll.shared.util.http;

import com.fpt.metroll.shared.exception.*;
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
        }

        log.info("Error status {} due to exception {}", status, ex.getClass().getName());

        if (exceptionLog) {
            ex.printStackTrace();
        }

        ErrorResponse error = ErrorResponse.builder().error(ex.getMessage()).build();
        return ResponseEntity.status(status).body(error);
    }
}
