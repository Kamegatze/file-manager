package com.kamegatze.file.manager.exception;

public class FileSystemExistByNameAndUserException extends RuntimeException{

    public FileSystemExistByNameAndUserException(String message) {
        super(message);
    }
}
