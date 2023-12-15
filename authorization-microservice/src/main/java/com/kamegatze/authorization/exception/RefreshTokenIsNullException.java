package com.kamegatze.authorization.exception;

public class RefreshTokenIsNullException extends Exception {
    public RefreshTokenIsNullException(String message) {
        super(message);
    }
}
