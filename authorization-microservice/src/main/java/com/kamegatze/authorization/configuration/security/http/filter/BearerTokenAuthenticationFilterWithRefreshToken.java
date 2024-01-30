package com.kamegatze.authorization.configuration.security.http.filter;

import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.repoitory.UsersRepository;
import com.kamegatze.authorization.service.JwtService;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;


public class BearerTokenAuthenticationFilterWithRefreshToken
    extends BearerTokenAuthenticationFilter {

    private final JwtService jwtService;
    private final JwtIssuerValidator jwtValidator;
    private final UsersRepository usersRepository;
    public BearerTokenAuthenticationFilterWithRefreshToken(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtIssuerValidator jwtValidator,
            UsersRepository usersRepository) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.jwtValidator = jwtValidator;
        this.usersRepository = usersRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> tokenAccessOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.Authorization.name()
                )
        );
        if(tokenAccessOptional.isPresent()) {
            final String tokenAccess =  tokenAccessOptional.get().substring(7);
            long timeEnd = 0;
            try {
                timeEnd = JWTParser.parse(tokenAccess).getJWTClaimsSet().getExpirationTime().getTime();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            long timeNow = new Date().getTime();

            if(timeNow >= timeEnd) {
                try {
                    refresh(request, response, filterChain);
                    return;
                } catch (JwtValidationException | RefreshTokenIsNullException | UserNotExistException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        filterChain.doFilter(request, response);
    }


    private void refresh(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws JwtValidationException, RefreshTokenIsNullException, UserNotExistException, IOException, ServletException {
        final String token = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.AuthorizationRefresh.name()
                )
        ).orElseThrow(() -> new RefreshTokenIsNullException("Token is not exist"));
        OAuth2TokenValidatorResult result = jwtValidator.validate(
                new Jwt(token,
                        jwtService.getIssuedAt(token),
                        jwtService.getExpiresAt(token),
                        jwtService.getHeaders(token),
                        jwtService.getClaims(token))
        );
        if (result.hasErrors()) {
            throw new InvalidBearerTokenException("Current refresh token is invalid");
        }
        if(!usersRepository.existsByLogin(jwtService.getLogin(token))) {
            throw new UserNotExistException("user with current login not exist");
        }

        filterChain.doFilter(request, response);
    }
}
