package com.kamegatze.authorization.configuration.security.http.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamegatze.authorization.configuration.security.details.UsersDetailsService;
import com.kamegatze.authorization.dto.ETokenType;
import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.repoitory.AuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersAuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersRepository;
import com.kamegatze.authorization.service.JwtService;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;


import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;


public class BearerTokenAuthenticationFilterWithRefreshToken
    extends BearerTokenAuthenticationFilter {

    private final JwtService jwtService;
    private final JwtIssuerValidator jwtValidator;
    private final UsersRepository usersRepository;
    private final UsersAuthorityRepository usersAuthorityRepository;
    private final AuthorityRepository authorityRepository;
    public BearerTokenAuthenticationFilterWithRefreshToken(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtIssuerValidator jwtValidator,
            UsersRepository usersRepository,
            UsersAuthorityRepository usersAuthorityRepository, AuthorityRepository authorityRepository) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.jwtValidator = jwtValidator;
        this.usersRepository = usersRepository;
        this.usersAuthorityRepository = usersAuthorityRepository;
        this.authorityRepository = authorityRepository;
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
                    refresh(request, response);
                    return;
                } catch (RefreshTokenIsNullException | UserNotExistException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.doFilterInternal(request, response, filterChain);
    }


    private void refresh(HttpServletRequest request, HttpServletResponse response) throws RefreshTokenIsNullException, UserNotExistException, IOException {
        final String token = Optional.of(
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
        String login = jwtService.getLogin(token);
        if(!usersRepository.existsByLogin(jwtService.getLogin(token))) {
            throw new UserNotExistException("user with current login not exist");
        }

        UsersDetailsService usersDetailsService = new UsersDetailsService(usersRepository, usersAuthorityRepository, authorityRepository);

        UserDetails usersDetails = usersDetailsService.loadUserByUsername(login);

        String tokenAccess = jwtService.generateAccess(usersDetails);
        String tokenRefresh = jwtService.generateRefresh(usersDetails);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(
                response.getOutputStream(),
                JwtDto.builder()
                        .refreshToken(tokenRefresh)
                        .tokenAccess(tokenAccess)
                        .type(ETokenType.Bearer)
                        .build()
        );
    }
}
