package com.kamegatze.file.manager.configuration.security.provider;

import com.kamegatze.file.manager.configuration.security.authentication.token.JwtRemoteAuthenticationToken;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.text.ParseException;
import java.util.NoSuchElementException;

public class JwtRemoteAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    public JwtRemoteAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JWT jwt = (JWT) authentication.getPrincipal();
        try {
            JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
            String login = jwtClaimsSet.getSubject();
            userDetailsService.loadUserByUsername(login);
            return authentication;
        } catch (ParseException | NoSuchElementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtRemoteAuthenticationToken.class);
    }
}
