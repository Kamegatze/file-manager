package com.kamegatze.authorization.exception;

import org.springframework.security.core.AuthenticationException;

public class Invalid2FaAuthenticationException extends AuthenticationException {
    public Invalid2FaAuthenticationException(String message) {
        super(message);
    }
}
