package com.kamegatze.file.manager.service;

import com.kamegatze.authorization.remote.security.converter.HeaderAuthentication;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface JwtService {
    String getLogin(HttpServletRequest request);
    Instant getIssuedAt(HttpServletRequest request);
    Instant getExpiresAt(HttpServletRequest request);
    Map<String, Object> getClaims(HttpServletRequest request);

    Map<String, Object> getHeaders(HttpServletRequest request);

    default String getToken(HttpServletRequest request) {
        Optional<String> getTokenOptional = Optional.ofNullable(
                request.getHeader(HeaderAuthentication.AUTHORIZATION.name())
        );
        return getTokenOptional.map(token -> token.substring(7))
                .orElseThrow(() -> new NoSuchElementException("Token not found in request"));
    }
}
