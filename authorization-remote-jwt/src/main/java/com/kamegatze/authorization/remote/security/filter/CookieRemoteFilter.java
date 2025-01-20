package com.kamegatze.authorization.remote.security.filter;

import com.kamegatze.authorization.remote.security.converter.CookieRemoteAuthenticationConverter;
import com.kamegatze.authorization.remote.security.http.entry.point.ExceptionEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
import java.util.Objects;

public class CookieRemoteFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    private final String cookieAccessName;
    private final String cookieRefreshName;

    public CookieRemoteFilter(AuthenticationManager authenticationManager,
                              HandlerExceptionResolver handlerExceptionResolver, String cookieAccessName, String cookieRefreshName) {
        this.authenticationManager = authenticationManager;
        this.authenticationFailureHandler = new AuthenticationEntryPointFailureHandler(
                new ExceptionEntryPoint(handlerExceptionResolver));
        this.cookieAccessName = cookieAccessName;
        this.cookieRefreshName = cookieRefreshName;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        CookieRemoteAuthenticationConverter cookieAuthenticationConverter = new CookieRemoteAuthenticationConverter();
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
            doFilter(request, response, filterChain);
        } catch (AuthenticationException failed) {
            securityContextHolderStrategy.clearContext();
            authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
        }
    }
}
