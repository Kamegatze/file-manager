package com.kamegatze.authorization.services;

import com.kamegatze.authorization.remote.security.jwt.JwtGenerate;

import java.time.Instant;
import java.util.Map;

public interface JwtService extends JwtGenerate {
    String getLogin(String token);

    Instant getIssuedAt(String token);

    Instant getExpiresAt(String token);

    Map<String, Object> getClaims(String token);

    Map<String, Object> getHeaders(String token);
}
