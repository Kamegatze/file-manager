package com.kamegatze.authorization.service.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.dto.ETokenType;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.model.Authority;
import com.kamegatze.authorization.model.EAuthority;
import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.model.UsersAuthority;
import com.kamegatze.authorization.repoitory.AuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersAuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersRepository;
import com.kamegatze.authorization.service.AuthorizationService;
import com.kamegatze.authorization.service.JwtService;
import com.nimbusds.jwt.JWTParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UsersRepository usersRepository;

    private final AuthorityRepository authorityRepository;

    private final UsersAuthorityRepository usersAuthorityRepository;

    private final ModelMapper model;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @Override
    public UsersDto signup(UsersDto usersDto) throws UsersExistException {
        Users users = model.map(usersDto, Users.class);

        Optional<Users> usersFind = usersRepository.findByLogin(users.getLogin());
        if(usersFind.isPresent()) {
            throw new UsersExistException(String.format("user with login: %s exist", users.getLogin()));
        }

        Authority authorityRead = authorityRepository.findByName(EAuthority.AUTHORITY_READ)
                .orElseThrow(() -> new NoSuchElementException("There are no such rights"));

        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users = usersRepository.save(users);
        usersAuthorityRepository.save(UsersAuthority.builder()
                        .authorityId(authorityRead.getId())
                        .usersId(users.getId())
                        .build());

        return model.map(users, UsersDto.class);
    }

    @Override
    public JwtDto signin(Login login) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getLogin(),
                        login.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String tokenAccess = jwtService.generateAccess((UsersDetails) authentication.getPrincipal());
        String tokenRefresh = jwtService.generateRefresh((UsersDetails) authentication.getPrincipal());

        return JwtDto.builder()
                .tokenAccess(tokenAccess)
                .refreshToken(tokenRefresh)
                .type(ETokenType.Bearer)
                .build();
    }

    @Override
    public Boolean isAuthenticationUser(String token) throws ParseException {
        if(token != null) {

            long expiresAtToken = JWTParser.parse(token).getJWTClaimsSet().getExpirationTime().getTime();
            long now = new Date().getTime();
            if(now <= expiresAtToken) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
