package com.kamegatze.authorization.remote.security.converter;

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
import java.util.Optional;

@Setter
@Getter
public class CookieAuthenticationConverter implements AuthenticationConverter {

    private Charset credentialsCharset;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    private String cookieName;

    public CookieAuthenticationConverter(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.credentialsCharset = StandardCharsets.UTF_8;
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public CookieAuthenticationConverter() {
        this(new WebAuthenticationDetailsSource());
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        Optional<Cookie> cookieOptional = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .findFirst();

        if (cookieOptional.isEmpty()) {
            return null;
        }
        String token = cookieOptional.get().getValue();

        return new BearerTokenAuthenticationToken(token);
    }
}
