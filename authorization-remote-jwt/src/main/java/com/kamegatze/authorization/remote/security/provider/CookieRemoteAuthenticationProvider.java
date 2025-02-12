package com.kamegatze.authorization.remote.security.provider;

import com.kamegatze.authorization.remote.security.authentication.token.JwtRemoteAuthenticationToken;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.text.ParseException;
import java.util.Set;

public class CookieRemoteAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;

    public CookieRemoteAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JWT token = (JWT) authentication.getCredentials();
        JWTClaimsSet jwt = parseJWT(token.getParsedString());
        String login = jwt.getSubject();
        userDetailsService.loadUserByUsername(login);
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtRemoteAuthenticationToken.class);
    }

    private JWTClaimsSet parseJWT(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                    jwt.getJWTClaimsSet(), Set.of("iss", "sub", "exp", "iat", "authority")
            );
            claimsVerifier.verify(jwt.getJWTClaimsSet(), new SimpleSecurityContext());
            return jwt.getJWTClaimsSet();
        } catch (ParseException | BadJWTException e) {
            throw new RuntimeException(e);
        }
    }
}
