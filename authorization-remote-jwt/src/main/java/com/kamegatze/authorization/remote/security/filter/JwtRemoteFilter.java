package com.kamegatze.authorization.remote.security.filter;

import com.kamegatze.authorization.remote.security.converter.HeaderAuthentication;
import com.kamegatze.authorization.remote.security.converter.JwtRemoteAuthenticationConverter;
import com.kamegatze.authorization.remote.security.exception.HttpInvalidJwtException;
import com.kamegatze.authorization.remote.security.expired.check.ExpiredCheck;
import com.kamegatze.authorization.remote.security.filter.model.Authority;
import com.kamegatze.authorization.remote.security.http.entry.point.ExceptionEntryPoint;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
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
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class JwtRemoteFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final RestOperations restOperations;
    private final String urlIsAuthentication;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    private final ExpiredCheck expiredCheck;

    public JwtRemoteFilter(AuthenticationManager authenticationManager,
                           RestOperations restOperations,
                           String urlIsAuthentication,
                           HandlerExceptionResolver handlerExceptionResolver,
                           ExpiredCheck expiredCheck) {
        this.authenticationManager = authenticationManager;
        this.restOperations = restOperations;
        this.urlIsAuthentication = urlIsAuthentication;
        this.expiredCheck = expiredCheck;
        this.authenticationFailureHandler = new AuthenticationEntryPointFailureHandler(
                new ExceptionEntryPoint(handlerExceptionResolver));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        Optional<Exception> runtimeException = validate(request);
        if(runtimeException.isPresent()) {
            authenticationFailureHandler.onAuthenticationFailure(request, response,
                    new InvalidBearerTokenException(runtimeException.get().getMessage(),
                    runtimeException.get()));
            return;
        }
        List<Authority> authorities;
        try {
            Optional<String> token = Optional.ofNullable(
                    request.getHeader(
                            HeaderAuthentication.AUTHORIZATION.value()
                    )
            ).map(authorizationToken -> authorizationToken.substring(7));
            if (token.isEmpty()) {
                doFilter(request, response, filterChain);
                return;
            }
            if (expiredCheck.check(token.get())) {
                authorities = getAuthoritiesFromToken(token.get());
            } else {
                authorities = getAuthorities(request);
            }
        }
        catch (HttpClientErrorException exception) {
            authenticationFailureHandler.onAuthenticationFailure(request, response,
                    new HttpInvalidJwtException(exception.getMessage(), exception));
            return;
        }
        AuthenticationConverter converter = new JwtRemoteAuthenticationConverter(authorities);
        Optional<Authentication> authenticationOptional = Optional.ofNullable(
                converter.convert(request)
        );

        if (authenticationOptional.isEmpty()) {
            doFilter(request, response, filterChain);
            return;
        }

        Authentication authentication = authenticationOptional.get();

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
            authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
        }
    }

    private List<Authority> getAuthoritiesFromToken(String token) {
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String[] authorityArray = ((String) jwtClaimsSet.getClaim("authority")).split(" ");
        return Arrays.asList(authorityArray).parallelStream()
                .map(item -> new Authority(item.trim())).toList();
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

    private Optional<Exception> validate(HttpServletRequest request) {
        Optional<String> authorizationHeaderOptional = Optional.ofNullable(
                request.getHeader(
                        HeaderAuthentication.AUTHORIZATION.value()
                )
        );
        if (authorizationHeaderOptional.isEmpty()) {
            return Optional.empty();
        }
        if (!authorizationHeaderOptional.get().startsWith("Bearer")) {
            return Optional.empty();
        }
        String authorizationHeader = authorizationHeaderOptional.get().substring(7);
        JWT jwt;
        try {
            jwt = JWTParser.parse(authorizationHeader);
            DefaultJWTClaimsVerifier<com.nimbusds.jose.proc.SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                    jwt.getJWTClaimsSet(), Set.of("iss", "sub", "exp", "iat", "authority")
            );
            claimsVerifier.verify(jwt.getJWTClaimsSet(), new SimpleSecurityContext());
        } catch (ParseException | BadJWTException e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }
}

