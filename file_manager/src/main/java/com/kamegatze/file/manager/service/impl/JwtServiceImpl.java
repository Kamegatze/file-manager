package com.kamegatze.file.manager.service.impl;

import com.kamegatze.file.manager.service.JwtService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${token.cookie.jwt.access-token.name}")
    private String cookieAccessName;

    @Override
    public String getLogin(HttpServletRequest request) {
        log.info("Begin get login from jwt token");
        String token = getToken(request, cookieAccessName);
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error parsing jwt: {}", e.toString());
            throw new RuntimeException(e);
        }
        log.info("End get login from jwt token");
        return jwtClaimsSet.getSubject();
    }

    @Override
    public Instant getIssuedAt(HttpServletRequest request) {
        log.info("Begin get issue from jwt token");
        String token = getToken(request, cookieAccessName);
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error parsing jwt: {}", e.toString());
            throw new RuntimeException(e);
        }
        log.info("End get issue from jwt token");
        return jwtClaimsSet.getIssueTime().toInstant();
    }

    @Override
    public Instant getExpiresAt(HttpServletRequest request) {
        log.info("Begin get expires from jwt token");
        JWTClaimsSet jwtClaimsSet;
        String token = getToken(request, cookieAccessName);
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error parsing jwt: {}", e.toString());
            throw new RuntimeException(e);
        }
        log.info("End get expires from jwt token");
        return jwtClaimsSet.getExpirationTime().toInstant();
    }

    @Override
    public Map<String, Object> getClaims(HttpServletRequest request) {
        log.info("Begin get claims from jwt token");
        JWTClaimsSet jwtClaimsSet;
        String token = getToken(request, cookieAccessName);
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error parsing jwt: {}", e.toString());
            throw new RuntimeException(e);
        }
        log.info("End get claims from jwt token");
        return jwtClaimsSet.getClaims();
    }

    @Override
    public Map<String, Object> getHeaders(HttpServletRequest request) {
        log.info("Begin get headers from jwt token");
        Map<String, Object> headers;
        String token = getToken(request, cookieAccessName);
        try {
            headers = JWTParser.parse(token).getHeader().toJSONObject();
        } catch (ParseException e) {
            log.error("Error parsing jwt: {}", e.toString());
            throw new RuntimeException(e);
        }
        log.info("Begin get headers from jwt token");
        return headers;
    }
}
