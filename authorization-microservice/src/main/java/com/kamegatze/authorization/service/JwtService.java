package com.kamegatze.authorization.service;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;

public interface JwtService {
    String generateAccess(UsersDetails usersDetails);
    String generateRefresh(UsersDetails usersDetails);
    String getLogin(String token);
    Instant getIssuedAt(String token);
    Instant getExpiresAt(String token);
    Map<String, Object> getClaims(String token);

    Map<String, Object> getHeaders(String token);
}
