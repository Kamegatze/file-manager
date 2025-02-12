package com.kamegatze.file.manager.service;

import com.kamegatze.authorization.remote.security.converter.HeaderAuthentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface JwtService {
    String getLogin(HttpServletRequest request);
    Instant getIssuedAt(HttpServletRequest request);
    Instant getExpiresAt(HttpServletRequest request);
    Map<String, Object> getClaims(HttpServletRequest request);

    Map<String, Object> getHeaders(HttpServletRequest request);

    default String getToken(HttpServletRequest request, String cookieName) {
        Optional<String> getTokenOptional = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst().map(Cookie::getValue);
        return getTokenOptional.orElseThrow(() -> new NoSuchElementException("Token not found in request"));
    }
}
