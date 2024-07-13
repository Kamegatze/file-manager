package com.kamegatze.authorization.exception;

public class QRCreateException extends RuntimeException {
    public QRCreateException (String message) {
        super(message);
    }
    public QRCreateException(String message, Throwable cause) {
        super(message, cause);
    }
    public QRCreateException(Throwable cause) {
        super(cause);
    }
}
