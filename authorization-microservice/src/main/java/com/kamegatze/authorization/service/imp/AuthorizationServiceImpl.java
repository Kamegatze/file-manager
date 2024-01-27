package com.kamegatze.authorization.service.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.configuration.security.details.UsersDetailsService;
import com.kamegatze.authorization.dto.ETokenType;
import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;


import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

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

    private final UsersDetailsService usersDetailsService;

    private final JwtIssuerValidator jwtValidator;

    @Override
    public UsersDto signup(UsersDto usersDto) throws UsersExistException {
        Users users = Users.builder()
                .password(usersDto.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();

        Optional<Users> usersFindByLogin = usersRepository.findByLogin(users.getLogin());
        if(usersFindByLogin.isPresent()) {
            throw new UsersExistException(String.format("user with login: %s exist", users.getLogin()));
        }

        Optional<Users> usersFindByEmail = usersRepository.findByEmail(users.getEmail());
        if (usersFindByEmail.isPresent()) {
            throw new UsersExistException(String.format("user with email: %s exist", users.getEmail()));
        }

        Authority authorityRead = authorityRepository.findByName(EAuthority.AUTHORITY_READ)
                .orElseThrow(() -> new NoSuchElementException("There are no such rights"));

        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users = usersRepository.save(users);
        usersAuthorityRepository.save(UsersAuthority.builder()
                        .authorityId(authorityRead.getId())
                        .usersId(users.getId())
                        .build());

        String[] name = users.getName().split(" ");
        return UsersDto.builder()
                .id(users.getId())
                .login(users.getLogin())
                .email(users.getEmail())
                .firstName(name[0])
                .lastName(name[1])
                .build();
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
    public Boolean isAuthenticationUser(HttpServletRequest request) throws ParseException {
        Optional<String> tokenAccessOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.Authorization.name()
                )
        );
        Optional<String> refreshTokenOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.AuthorizationRefresh.name()
                )
        );
        if (tokenAccessOptional.isEmpty() && refreshTokenOptional.isEmpty()) {
            return Boolean.FALSE;
        }

        String tokenRefresh = refreshTokenOptional.get();
        return tokenValid(tokenRefresh);
    }

    private Boolean tokenValid(String token) throws ParseException {
        try {
            OAuth2TokenValidatorResult result = jwtValidator.validate(
                    new Jwt(token,
                            jwtService.getIssuedAt(token),
                            jwtService.getExpiresAt(token),
                            jwtService.getHeaders(token),
                            jwtService.getClaims(token))
            );
            if (result.hasErrors()) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (JwtValidationException exception) {
            return Boolean.FALSE;
        }
    }

    @Override
    public JwtDto authenticationViaRefreshToken(HttpServletRequest request)
            throws RefreshTokenIsNullException, ParseException, InvalidBearerTokenException, UserNotExistException {
        Optional<String> tokenRefreshOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.AuthorizationRefresh.name()
                )
        );

        if (tokenRefreshOptional.isEmpty()) {
            throw new RefreshTokenIsNullException("Refresh token is null");
        }

        String token = tokenRefreshOptional.get();

        if (!tokenValid(token)) {
            throw new InvalidBearerTokenException("Refresh token invalid");
        }
        String login = jwtService.getLogin(token);
        if (!usersRepository.existsByLogin(login)) {
            throw new UserNotExistException(String.format("User with login: [%s] not exist", login));
        }

        String accessToken = jwtService.generateAccess(usersDetailsService.loadUserByUsername(login));

        return JwtDto.builder()
                .refreshToken(token)
                .tokenAccess(accessToken)
                .type(ETokenType.Bearer)
                .build();
    }

    @Override
    public Boolean isExistUser(String loginOrEmail) {
        final String emailRegExp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        boolean isEmail = Pattern.compile(emailRegExp).matcher(loginOrEmail).matches();
        if (isEmail) {
            return usersRepository.existsByEmail(loginOrEmail);
        }
        return usersRepository.existsByLogin(loginOrEmail);
    }
}
