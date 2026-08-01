package org.com.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.com.gateway.model.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthenticationExceptionHandler {

    private static final String GENERIC_MESSAGE = "Invalid credentials";

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthenticationFailure(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                GENERIC_MESSAGE,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
