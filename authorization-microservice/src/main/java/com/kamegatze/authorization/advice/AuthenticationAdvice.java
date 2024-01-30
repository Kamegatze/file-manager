package com.kamegatze.authorization.advice;

import com.kamegatze.authorization.controllers.AuthenticationController;
import com.kamegatze.authorization.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = AuthenticationController.class)
public class AuthenticationAdvice {
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<Response> handleBadCredentialsException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(HttpStatus.UNAUTHORIZED.value())
                        .message(e.getMessage())
                        .build());
    }
}
