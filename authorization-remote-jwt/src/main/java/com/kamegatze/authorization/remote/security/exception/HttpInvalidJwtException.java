package com.kamegatze.authorization.remote.security.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.client.HttpClientErrorException;

public class HttpInvalidJwtException extends AuthenticationException {
    private final HttpClientErrorException httpClientErrorException;

    public HttpInvalidJwtException(String msg, Throwable cause) {
        super(msg, cause);
        this.httpClientErrorException = (HttpClientErrorException) cause;
    }

    public byte[] getResponseBodyAsByteArray() {
        return this.httpClientErrorException.getResponseBodyAsByteArray();
    }

    public String getResponseBodyAsString() {
        return this.httpClientErrorException.getResponseBodyAsString();
    }
}
