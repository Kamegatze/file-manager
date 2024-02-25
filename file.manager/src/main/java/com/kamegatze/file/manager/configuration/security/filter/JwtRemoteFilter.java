package com.kamegatze.file.manager.configuration.security.filter;


import com.kamegatze.file.manager.configuration.security.converter.JwtRemoteAuthenticationConverter;
import com.kamegatze.file.manager.configuration.security.filter.model.Authority;
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
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class JwtRemoteFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final RestOperations restOperations;
    public JwtRemoteFilter(AuthenticationManager authenticationManager, RestOperations restOperations) {
        this.authenticationManager = authenticationManager;
        this.restOperations = restOperations;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        AuthenticationConverter converter = new JwtRemoteAuthenticationConverter();
        Optional<Authentication> authenticationOptional = Optional.ofNullable(
                converter.convert(request)
        );
        if (authenticationOptional.isEmpty()) {
            doFilter(request, response, filterChain);
            return;
        }
        Authentication authentication = authenticationOptional.get();
        //todo убрать в отдельный метод формирование MultiValueMap<String, String>
        Enumeration<String> nameOfHeaders = request.getHeaderNames();
        MultiValueMap<String, String> headers = new HttpHeaders();
        if (nameOfHeaders != null) {
            while (nameOfHeaders.hasMoreElements()) {
                String nameOfHeader = nameOfHeaders.nextElement();
                headers.add(nameOfHeader, request.getHeader(nameOfHeader));
            }
        }

        List<Authority> authorities = restOperations.exchange(
                "http://localhost:8080/api/authentication/micro-service/is-authentication",
                HttpMethod.GET, new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Authority>>() {}
        ).getBody();

        if (authorities != null) {
            doFilter(request, response, filterChain);
            return;
        }
        if (authorities.isEmpty()) {
            doFilter(request, response, filterChain);
            return;
        }


        doFilter(request, response, filterChain);
    }
}
