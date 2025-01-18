package com.kamegatze.authorization.remote.security.converter;

import com.kamegatze.authorization.remote.security.jwt.JwtUtility;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Setter
@Getter
public class CookieAuthenticationConverter implements AuthenticationConverter {

    private Charset credentialsCharset;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    private String cookieAccessName;
    private String cookieRefreshName;

    public CookieAuthenticationConverter(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.credentialsCharset = StandardCharsets.UTF_8;
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public CookieAuthenticationConverter() {
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
            return new BearerTokenAuthenticationToken(refreshToken.getValue());
        }
        return new BearerTokenAuthenticationToken(accessToken.getValue());
    }
}
