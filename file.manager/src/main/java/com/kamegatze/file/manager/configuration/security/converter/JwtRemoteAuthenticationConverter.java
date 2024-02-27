package com.kamegatze.file.manager.configuration.security.converter;

import com.kamegatze.file.manager.configuration.security.authentication.token.JwtRemoteAuthenticationToken;
import com.kamegatze.file.manager.configuration.security.filter.model.Authority;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JwtRemoteAuthenticationConverter implements AuthenticationConverter {

    public static final String AUTHENTICATION_SCHEME_BASIC = "JwtRemote";
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    private Charset credentialsCharset;

    private List<Authority> authorities;

    public JwtRemoteAuthenticationConverter() {
        this(new WebAuthenticationDetailsSource());
    }

    public JwtRemoteAuthenticationConverter(List<Authority> authorities) {
        this(new WebAuthenticationDetailsSource());
        this.authorities = authorities;
    }

    public JwtRemoteAuthenticationConverter(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.credentialsCharset = StandardCharsets.UTF_8;
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public void setCredentialsCharset(Charset credentialsCharset) {
        this.credentialsCharset = credentialsCharset;
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
        if (!authorizationHeaderOptional.get().startsWith("Bearer")) {
            return null;
        }
        String authorizationHeader = authorizationHeaderOptional.get().substring(7);
        JWT jwt;
        try {
            jwt = JWTParser.parse(authorizationHeader);
            DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                jwt.getJWTClaimsSet(), Set.of("iss", "sub", "exp", "iat", "authority")
            );
            claimsVerifier.verify(jwt.getJWTClaimsSet(), new SimpleSecurityContext());
        } catch (ParseException | BadJWTException e) {
            throw new RuntimeException(e);
        }
        List<SimpleGrantedAuthority> grantedAuthorityList = this.authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName())).toList();
        JwtRemoteAuthenticationToken<JWT> authenticationToken = new JwtRemoteAuthenticationToken<>(jwt, grantedAuthorityList);
        authenticationToken.setDetails(authenticationDetailsSource.buildDetails(request));
        return authenticationToken;
    }
}
