package com.kamegatze.authorization.remote.security.filter;

import com.kamegatze.authorization.remote.security.converter.CookieAuthenticationConverter;
import com.kamegatze.authorization.remote.security.http.entry.point.ExceptionEntryPoint;
import com.kamegatze.authorization.remote.security.jwt.JwtGenerate;
import com.kamegatze.authorization.remote.security.jwt.JwtUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


public class CookieFilter extends OncePerRequestFilter {

    private final String cookieAccessName;
    private final String cookieRefreshName;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();
    private final JwtGenerate jwtGenerate;

    public CookieFilter(String cookieAccessName, String cookieRefreshName, AuthenticationManager authenticationManager, HandlerExceptionResolver handlerExceptionResolver, JwtGenerate jwtGenerate) {
        this.cookieAccessName = cookieAccessName;
        this.cookieRefreshName = cookieRefreshName;
        this.authenticationManager = authenticationManager;
        this.authenticationFailureHandler = new AuthenticationEntryPointFailureHandler(
                new ExceptionEntryPoint(handlerExceptionResolver));
        this.jwtGenerate = jwtGenerate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CookieAuthenticationConverter cookieAuthenticationConverter = new CookieAuthenticationConverter();
        cookieAuthenticationConverter.setCookieAccessName(cookieAccessName);
        cookieAuthenticationConverter.setCookieRefreshName(cookieRefreshName);
        Authentication authentication = cookieAuthenticationConverter.convert(request);
        if (Objects.isNull(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Authentication authResult = authenticationManager.authenticate(authentication);
            SecurityContext securityContext = securityContextHolderStrategy.createEmptyContext();
            securityContext.setAuthentication(authResult);
            securityContextHolderStrategy.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, request, response);
            updateTokens(request, response);
            doFilter(request, response, filterChain);
        } catch (AuthenticationException failed) {
            securityContextHolderStrategy.clearContext();
            authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
        }
    }

    private void updateTokens(HttpServletRequest request, HttpServletResponse response) {
        if (Objects.isNull(request.getCookies())) {
            return;
        }

        Optional<Cookie> cookieAccessOptional = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieAccessName))
                .findFirst();

        Optional<Cookie> cookieRefreshOptional = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieRefreshName))
                .findFirst();

        if (cookieAccessOptional.isEmpty() || cookieRefreshOptional.isEmpty()) {
            return;
        }

        Cookie accessCookie = cookieAccessOptional.get();
        Cookie refreshCookie = cookieRefreshOptional.get();

        if (JwtUtility.isExpired(accessCookie.getValue())) {
            if (JwtUtility.isExpired(refreshCookie.getValue())) {
                return;
            }

            accessCookie.setValue(jwtGenerate.generateAccess(
                    JwtUtility.getUsernameFromToken(accessCookie.getValue()),
                    JwtUtility.getAuthoritiesFromToken(accessCookie.getValue())
                            .stream().map(authority -> ((GrantedAuthority) new SimpleGrantedAuthority(authority.getName()))).toList()
            ));
            refreshCookie.setValue(jwtGenerate.generateRefresh(
                    JwtUtility.getUsernameFromToken(refreshCookie.getValue()),
                    JwtUtility.getAuthoritiesFromToken(refreshCookie.getValue())
                            .stream().map(authority -> ((GrantedAuthority) new SimpleGrantedAuthority(authority.getName()))).toList()
            ));

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);
        }
    }
}
