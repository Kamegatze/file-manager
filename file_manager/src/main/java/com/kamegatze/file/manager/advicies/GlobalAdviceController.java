package com.kamegatze.file.manager.advicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamegatze.authorization.remote.security.exception.HttpInvalidJwtException;
import com.kamegatze.general.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalAdviceController {

    private final ObjectMapper objectMapper;

    @ExceptionHandler({HttpInvalidJwtException.class})
    public ResponseEntity<ResponseDto> handleHttpClientErrorException(HttpInvalidJwtException exception) throws JsonProcessingException {
        String body = exception.getResponseBodyAsString();
        ResponseDto responseFromAuthorizationService = objectMapper.readValue(
                body,
                ResponseDto.class
        );
        ResponseDto responseDto = ResponseDto.builder()
                .message(responseFromAuthorizationService.getMessage())
                .status(responseFromAuthorizationService.getStatus())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    @ExceptionHandler({InvalidBearerTokenException.class})
    public ResponseEntity<ResponseDto> handleInvalidBearerTokenException(InvalidBearerTokenException exception) {
        ResponseDto responseDto = ResponseDto.builder()
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }
}
