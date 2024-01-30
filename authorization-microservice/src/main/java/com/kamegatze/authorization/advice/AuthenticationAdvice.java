package com.kamegatze.authorization.advice;

import com.kamegatze.authorization.controllers.AuthenticationController;
import com.kamegatze.authorization.dto.Response;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
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

    @ExceptionHandler({UserNotExistException.class})
    public ResponseEntity<Response> handleUserNotExistException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({RefreshTokenIsNullException.class})
    public ResponseEntity<Response> handleRefreshTokenIsNullException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({InvalidBearerTokenException.class})
    public ResponseEntity<Response> handleInvalidBearerTokenException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({NotEqualsPasswordException.class})
    public ResponseEntity<Response> handleNotEqualsPasswordException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({MessagingException.class})
    public ResponseEntity<Response> handleMessagingException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .build());
    }
}
