package com.kamegatze.file.manager.advicies;

import com.kamegatze.file.manager.controllers.FileSystemController;
import com.kamegatze.file.manager.exception.FileSystemExistByNameAndUserException;
import com.kamegatze.general.dto.response.ResponseDto;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice(assignableTypes = FileSystemController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FileSystemAdviceController {

    @ExceptionHandler({FileSystemExistByNameAndUserException.class})
    public ResponseEntity<ResponseDto> handleFileSystemExistByNameAndUserException(FileSystemExistByNameAndUserException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<ResponseDto> handleNoSuchElementException(NoSuchElementException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
