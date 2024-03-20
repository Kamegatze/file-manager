package com.kamegatze.authorization.advice;

import com.kamegatze.authorization.controllers.AuthenticationController;
import com.kamegatze.authorization.exception.EqualsPasswordException;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.general.dto.response.ResponseDto;
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
    public ResponseEntity<ResponseDto> handleBadCredentialsException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({UserNotExistException.class})
    public ResponseEntity<ResponseDto> handleUserNotExistException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({RefreshTokenIsNullException.class})
    public ResponseEntity<ResponseDto> handleRefreshTokenIsNullException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({InvalidBearerTokenException.class})
    public ResponseEntity<ResponseDto> handleInvalidBearerTokenException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({NotEqualsPasswordException.class})
    public ResponseEntity<ResponseDto> handleNotEqualsPasswordException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({MessagingException.class})
    public ResponseEntity<ResponseDto> handleMessagingException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({UsersExistException.class})
    public ResponseEntity<ResponseDto> handleUsersExistException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    ResponseDto.builder()
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build()
                );
    }

    @ExceptionHandler({EqualsPasswordException.class})
    public ResponseEntity<ResponseDto> handleEqualsPasswordException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        ResponseDto.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }
}
