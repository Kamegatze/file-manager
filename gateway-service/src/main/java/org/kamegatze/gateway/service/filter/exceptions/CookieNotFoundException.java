package org.kamegatze.gateway.service.filter.exceptions;

public class CookieNotFoundException extends RuntimeException {

    public CookieNotFoundException(Throwable cause) {
        super(cause);
    }

    public CookieNotFoundException(String message) {
        super(message);
    }
}
