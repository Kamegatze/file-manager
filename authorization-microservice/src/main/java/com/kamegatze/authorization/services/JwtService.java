package com.kamegatze.authorization.services;

import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Map;

public interface JwtService {
    String generateAccess(UserDetails usersDetails);
    String generateRefresh(UserDetails usersDetails);
    String getLogin(String token);
    Instant getIssuedAt(String token);
    Instant getExpiresAt(String token);
    Map<String, Object> getClaims(String token);

    Map<String, Object> getHeaders(String token);
}
