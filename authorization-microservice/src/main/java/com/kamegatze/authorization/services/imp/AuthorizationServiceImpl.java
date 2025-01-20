package com.kamegatze.authorization.services.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.configuration.security.details.UsersDetailsService;
import com.kamegatze.authorization.dto.*;
import com.kamegatze.authorization.dto.mfa.MFADto;
import com.kamegatze.authorization.exception.Invalid2FaAuthenticationException;
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
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.authorization.services.JwtService;
import com.kamegatze.authorization.services.MFATokenService;
import com.kamegatze.authorization.transfer.client.ClientTransfer;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UsersRepository usersRepository;
    private final AuthorityRepository authorityRepository;
    private final UsersAuthorityRepository usersAuthorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersDetailsService usersDetailsService;
    private final JwtIssuerValidator jwtValidator;
    private final ClientTransfer<Object> clientTransfer;
    private final MFATokenService mfaTokenService;
    private final CookieProperties cookieProperties;


    @Value("${spring.kafka.topics.save.users}")
    private String topicSaveUsers;

    @Override
    public UsersDto signup(UsersDto usersDto) throws UsersExistException {
        Users users = Users.builder()
                .password(usersDto.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();

        Optional<Users> usersFindByLogin = usersRepository.findByLogin(users.getLogin());
        if (usersFindByLogin.isPresent()) {
            throw new UsersExistException(String.format("User with login: \"%s\" exist", users.getLogin()));
        }

        Optional<Users> usersFindByEmail = usersRepository.findByEmail(users.getEmail());
        if (usersFindByEmail.isPresent()) {
            throw new UsersExistException(String.format("User with email: \"%s\" exist", users.getEmail()));
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
        UsersDto usersDtoResult = UsersDto.builder()
                .id(users.getId())
                .login(users.getLogin())
                .email(users.getEmail())
                .firstName(name[0])
                .lastName(name[1])
                .build();
        clientTransfer.sendData(usersDtoResult, topicSaveUsers);
        return usersDtoResult;
    }

    @Override
    public JwtDto signin(Login login, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getLogin(),
                        login.getCredentials()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String tokenAccess = jwtService.generateAccess((UsersDetails) authentication.getPrincipal());
        String tokenRefresh = jwtService.generateRefresh((UsersDetails) authentication.getPrincipal());

        setCookie(cookieProperties.getAccessToken(), response, tokenAccess);
        setCookie(cookieProperties.getRefreshToken(), response, tokenRefresh);

        return JwtDto.builder()
                .tokenAccess(tokenAccess)
                .refreshToken(tokenRefresh)
                .type(ETokenType.Bearer)
                .build();
    }

    private void setCookie(CookieInfo cookieInfo, HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(cookieInfo.getName(), value);
        cookie.setPath(cookieInfo.getPath());
        cookie.setDomain(cookieInfo.getDomain());
        cookie.setSecure(cookieInfo.isSecure());
        cookie.setHttpOnly(cookieInfo.isHttpOnly());
        cookie.setMaxAge(cookieInfo.getMaxAge() * 60);
        response.addCookie(cookie);
    }

    @Override
    public Boolean isAuthenticationUser(HttpServletRequest request) throws ParseException {
        Optional<String> tokenAccessOptional = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieProperties.getAccessToken().getName()))
                .findFirst()
                .map(Cookie::getValue);

        Optional<String> refreshTokenOptional = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieProperties.getRefreshToken().getName()))
                .findFirst()
                .map(Cookie::getValue);

        if (tokenAccessOptional.isEmpty() && refreshTokenOptional.isEmpty()) {
            return Boolean.FALSE;
        }

        if (refreshTokenOptional.isPresent() && tokenAccessOptional.isEmpty()) {
            String tokenRefresh = refreshTokenOptional.get();
            return tokenValid(tokenRefresh);
        }
        String token = tokenAccessOptional.get();

        if (refreshTokenOptional.isEmpty()) {
            return tokenValid(token);
        }

        return tokenValid(token) && tokenValid(refreshTokenOptional.get());
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
        } catch (JwtException exception) {
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
        UserDetails userDetails = usersDetailsService.loadUserByUsername(login);
        String accessToken = jwtService.generateAccess(userDetails);
        String refreshToken = jwtService.generateRefresh(userDetails);
        return JwtDto.builder()
                .refreshToken(refreshToken)
                .tokenAccess(accessToken)
                .type(ETokenType.Bearer)
                .build();
    }

    @Override
    public Boolean isExistUser(String loginOrEmail) {
        String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        boolean isEmail = Pattern.compile(EMAIL_PATTERN).matcher(loginOrEmail).matches();
        if (isEmail) {
            return usersRepository.existsByEmail(loginOrEmail);
        }
        return usersRepository.existsByLogin(loginOrEmail);
    }

    @Override
    public void isUserValidateAuthenticationCode(String code, String login) {
        Optional<Users> users = usersRepository.findByLogin(login);
        users.ifPresent((Users user) -> {
            boolean isValidateAuthenticationCode = mfaTokenService.verifyTotp(code, user.getSecret());
            if (!isValidateAuthenticationCode) {
                throw new Invalid2FaAuthenticationException("Invalid authentication");
            }
        });
        if (users.isEmpty()) {
            throw new NoSuchElementException(String.format("User not found by login \"%s\"", login));
        }
    }

    @Override
    public void changePassword(ChangePasswordDto changePasswordDto) {

    }

    @Override
    public List<AuthorityDto> getAuthorityByRequest(HttpServletRequest request) throws ParseException {
        Optional<String> headerOptional = Optional.ofNullable(
                request.getHeader(ETypeTokenHeader.Authorization.name())
        );
        if (headerOptional.isEmpty()) {
            return List.of();
        }
        String header = headerOptional.get().substring(7);
        JWTClaimsSet claimsSet = JWTParser.parse(header).getJWTClaimsSet();
        List<Authority> authorities = usersRepository.findByLogin(claimsSet.getSubject()).orElseThrow(
                () -> new UserNotExistException(String.format("User with login: [%s] not exist", claimsSet.getSubject()))
        ).getAuthorities();
        return authorities.stream().map(
                authority -> new AuthorityDto(authority.getName().name())
        ).toList();
    }

    @Override
    public MFADto set2FAAuthentication(HttpServletRequest request) {
        Users users = getUserViaHttpRequest(request);
        String secret = mfaTokenService.generateSecretKey();
        users.setSecret(secret);
        usersRepository.save(users);
        return new MFADto(mfaTokenService.getQRCode(secret), secret);
    }

    @Override
    public void checkMFAValidateCodeAndEnableAuthorizationViaMFA(String code, HttpServletRequest request) {
        Users users = getUserViaHttpRequest(request);
        users.setEnable2fa(true);
        isUserValidateAuthenticationCode(code, users.getLogin());
        usersRepository.save(users);
    }

    @Override
    public InfoAboutUser getInfoAboutUserByLogin(String login) {
        Users users = usersRepository.findByLogin(login)
                .orElseThrow(
                        () -> new UserNotExistException(String.format("User with login: [%s] not exist", login))
                );

        return new InfoAboutUser(users.getLogin(), users.isEnable2fa());
    }

    private Users getUserViaHttpRequest(HttpServletRequest request) {
        String jwt = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(cookieProperties.getAccessToken().getName()))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Jwt is null")).getValue();
        String login = jwtService.getLogin(jwt);
        return usersRepository.findByLogin(login).orElseThrow(
                () -> new UserNotExistException(String.format("User with login: [%s] not exist", login))
        );
    }
}
