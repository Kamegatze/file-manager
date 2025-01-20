package com.kamegatze.authorization.remote.security.converter;

import com.kamegatze.authorization.remote.security.authentication.token.JwtRemoteAuthenticationToken;
import com.kamegatze.authorization.remote.security.jwt.JwtUtility;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

@Data
public class CookieRemoteAuthenticationConverter implements AuthenticationConverter {
    private Charset credentialsCharset;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

    private String cookieAccessName;
    private String cookieRefreshName;

    public CookieRemoteAuthenticationConverter(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.credentialsCharset = StandardCharsets.UTF_8;
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public CookieRemoteAuthenticationConverter() {
        this(new WebAuthenticationDetailsSource());
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (Objects.isNull(request.getCookies())) {
            return null;
        }

        Optional<Cookie> cookieAccessOptional = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(cookieAccessName)).findFirst();

        Optional<Cookie> cookieRefreshOptional = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(cookieRefreshName)).findFirst();

        if (cookieAccessOptional.isEmpty() || cookieRefreshOptional.isEmpty()) {
            return null;
        }

        Cookie accessToken = cookieAccessOptional.get();
        Cookie refreshToken = cookieRefreshOptional.get();

        if (JwtUtility.isExpired(accessToken.getValue())) {
            if (JwtUtility.isExpired(refreshToken.getValue())) {
                return null;
            }
            JWT jwt = parseJWT(refreshToken.getValue());
            return new JwtRemoteAuthenticationToken<>(jwt,
                    JwtUtility.getAuthoritiesFromToken(refreshToken.getValue())
                            .stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                            .toList()
                    );
        }
        JWT jwt = parseJWT(accessToken.getValue());
        return new JwtRemoteAuthenticationToken<>(jwt,
                JwtUtility.getAuthoritiesFromToken(accessToken.getValue())
                        .stream()
                        .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                        .toList()
        );
    }

    private JWT parseJWT(String token) {
        try {
            return JWTParser.parse(token);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
