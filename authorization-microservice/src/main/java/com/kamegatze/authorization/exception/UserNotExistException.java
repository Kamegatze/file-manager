package com.kamegatze.authorization.exception;

public class UserNotExistException extends Exception {
    public UserNotExistException(String message) {
        super(message);
    }
}
