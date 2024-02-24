package com.kamegatze.file.manager.configuration.security.converter;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;

public class JwtRemoteAuthenticationConverter implements AuthenticationConverter {

    public static final String AUTHENTICATION_SCHEME_BASIC = "JwtRemote";
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    @Setter
    private Charset credentialsCharset;

    public JwtRemoteAuthenticationConverter() {
        this(new WebAuthenticationDetailsSource());
    }

    public JwtRemoteAuthenticationConverter(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.credentialsCharset = StandardCharsets.UTF_8;
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        Optional<String> authorizationHeaderOptional = Optional.ofNullable(
                request.getHeader(
                        HeaderAuthentication.AUTHORIZATION.name()
                )
        );
        if (authorizationHeaderOptional.isEmpty()) {
            return null;
        }
        String authorizationHeader = authorizationHeaderOptional.get().substring(7);
        try {
            JWTClaimsSet claimsSet = JWTParser.parse(authorizationHeader).getJWTClaimsSet();
            DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                claimsSet, Set.of("iss", "sub", "exp", "iat", "authority")
            );
            claimsVerifier.verify(claimsSet, new SimpleSecurityContext());
        } catch (ParseException | BadJWTException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
