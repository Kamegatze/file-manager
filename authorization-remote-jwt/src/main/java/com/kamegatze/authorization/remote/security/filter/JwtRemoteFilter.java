package com.kamegatze.authorization.remote.security.filter;

import com.kamegatze.authorization.remote.security.converter.JwtRemoteAuthenticationConverter;
import com.kamegatze.authorization.remote.security.filter.model.Authority;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class JwtRemoteFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final RestOperations restOperations;

    private final String urlIsAuthentication;

    private final AuthenticationEntryPoint authenticationEntryPoint = new Http403ForbiddenEntryPoint();
    private final AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationEntryPointFailureHandler(
            this.authenticationEntryPoint);
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();
    public JwtRemoteFilter(AuthenticationManager authenticationManager, RestOperations restOperations, String urlIsAuthentication) {
        this.authenticationManager = authenticationManager;
        this.restOperations = restOperations;
        this.urlIsAuthentication = urlIsAuthentication;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        AuthenticationConverter converter = new JwtRemoteAuthenticationConverter(getAuthorities(request));
        Optional<Authentication> authenticationOptional = Optional.ofNullable(
                converter.convert(request)
        );
        if (authenticationOptional.isEmpty()) {
            doFilter(request, response, filterChain);
            return;
        }

        Authentication authentication = authenticationOptional.get();

        List<Authority> authorities = getAuthorities(request);
        if (authorities == null || authorities.isEmpty()) {
            doFilter(request, response, filterChain);
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
            authenticationFailureHandler.onAuthenticationFailure(request,response, failed);
        }
    }

    private MultiValueMap<String, String> getHttpHeaders(HttpServletRequest request) {
        Enumeration<String> nameOfHeaders = request.getHeaderNames();
        MultiValueMap<String, String> headers = new HttpHeaders();
        if (nameOfHeaders != null) {
            while (nameOfHeaders.hasMoreElements()) {
                String nameOfHeader = nameOfHeaders.nextElement();
                headers.add(nameOfHeader, request.getHeader(nameOfHeader));
            }
        }
        return headers;
    }

    private List<Authority> getAuthorities(HttpServletRequest request) {
        return restOperations.exchange(
                urlIsAuthentication,
                HttpMethod.GET, new HttpEntity<>(getHttpHeaders(request)),
                new ParameterizedTypeReference<List<Authority>>() {}
        ).getBody();
    }
}
