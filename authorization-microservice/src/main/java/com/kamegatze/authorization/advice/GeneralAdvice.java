package com.kamegatze.authorization.advice;


import com.kamegatze.authorization.dto.Response;
import com.kamegatze.general.dto.response.ResponseDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GeneralAdvice {
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ResponseDto> handleAuthenticationException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ResponseDto> handleConstraintViolationException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message(e.getMessage())
                        .build());
    }
}
