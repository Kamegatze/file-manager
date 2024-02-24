package com.kamegatze.file.manager.configuration.security.filter;


import com.kamegatze.file.manager.configuration.security.converter.JwtRemoteAuthenticationConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtRemoteFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        AuthenticationConverter converter = new JwtRemoteAuthenticationConverter();
        converter.convert(request);

        doFilter(request, response, filterChain);
    }
}
