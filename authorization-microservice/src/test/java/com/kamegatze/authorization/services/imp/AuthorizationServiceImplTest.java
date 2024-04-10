package com.kamegatze.authorization.services.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.configuration.security.details.UsersDetailsService;
import com.kamegatze.authorization.dto.AuthorityDto;
import com.kamegatze.authorization.dto.ChangePasswordDto;
import com.kamegatze.authorization.dto.ETokenType;
import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.EqualsPasswordException;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.model.Authority;
import com.kamegatze.authorization.model.EAuthority;
import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.repoitory.AuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersAuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersRepository;
import com.kamegatze.authorization.services.EmailService;
import com.kamegatze.authorization.services.JwtService;
import com.kamegatze.authorization.transfer.client.ClientTransfer;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.ParseException;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тест класса авторизационного сервиса")
class AuthorizationServiceImplTest {
    @Mock
    UsersRepository usersRepository;
    @Mock
    AuthorityRepository authorityRepository;
    @Mock
    UsersAuthorityRepository usersAuthorityRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock
    UsersDetailsService usersDetailsService;
    @Mock
    JwtIssuerValidator jwtValidator;
    @Mock
    SpringTemplateEngine templateEngine;
    @Mock
    EmailService emailService;
    @Mock
    ClientTransfer<Object> clientTransfer;
    @InjectMocks
    AuthorizationServiceImpl authorizationService;
    String accessToken;
    String refreshToken;

    @BeforeEach
    void init() {
        accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRob3JpemF0aW9uLWZpbGUtbWFu" +
                "YWdlciIsInN1YiI6ImthbWVnYXR6ZSIsImV4cCI6MTcxMTIxNjEyMywiaWF0IjoxNzEx" +
                "MjE0MzIzLCJhdXRob3JpdHkiOiJBVVRIT1JJVFlfUkVBRCAifQ.wJ7eqQYlPm5xXBvht" +
                "YnJwkiUOPMSH3Rxw4tZIRgcns_bW0aD42bjDnuquQ4GDrQy8Yisaq0PdcKkm2haJBK590fV" +
                "7PfDVp0DrR7dGEekW1XGx9NiGG2uHjIp_HrG1SYxSueDZgitsLotw4eMjAeJrxf1pNu3k_Il" +
                "YJLfwaPszJNlEPgypgYAbHwxUP1_ZcyEsvuhHq1IcJUcOd9Rr2j0svssCb2etWombe8C9ehU0" +
                "jkJO-9FQXZimQJ3qMfzXvi7tGDCJYjuuqyU-FDIaLlF9zTdoIc6dBlWcith7W0QNNWIGJlGRyD" +
                "zsDw5ZGKb5dbQIVmk_00XiG8cuNkLCeVDgQ";
        refreshToken = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRob3JpemF0aW9uLWZpbGUtbWFuYWdlciIs" +
                "InN1YiI6ImthbWVnYXR6ZSIsImV4cCI6MTcxMTMwMDcyMywiaWF0IjoxNzExMjE0MzIzLCJhdXRob" +
                "3JpdHkiOiJBVVRIT1JJVFlfUkVBRCAifQ.k3VdazbcfZE1lhX5m4dJoHcnEHxJ-QvKSQwM0xHdaBfr" +
                "QdrXoX_N4pMXspeGLpoD-0QgBjBSNEXTyvK2_ApxIZyKha1ArwydXxQymgP_NRp8dwHL5MklH4Gb95W" +
                "_srqlMckbWCJxGKQcBhpoCjbjRw1Rkf9mvmLelHJrEMn9ZatNoIHfJ8NIdTHmZbJZGL0gF8K-Rwlnvpw" +
                "EnrvWcVN6sXFKntuiHmOeDjbAewLBDVSNXtlqGVwh-yB7RpKvc31Qf5B7WSfV_cwYIVZORBbK3RBAMKs" +
                "jr5iHJBhEl6kzQSB7uxmqfhwrlB2WO3i3rvQtg9ch7IZHgHKH0zh_nFk73A";
    }

    @Test
    @DisplayName("Тест создания пользователя. Положительный сценарий")
    void signup_ExecuteIsValid_CreateUserInSystem() throws UsersExistException {
        //given
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        UsersDto usersDto = UsersDto.builder()
                .login("kamegatze")
                .email("aleksi.aleksi2014@yandex.ru")
                .firstName("Aleksey")
                .lastName("Shirayev")
                .password("fgrgdddsdvbhgvbbfgrewert")
                .build();
        Users users = Users.builder()
                .password(usersDto.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();
        //поиск пользователя по логину
        doReturn(Optional.empty()).when(usersRepository).findByLogin(users.getLogin());
        //поиск пользователя по email
        doReturn(Optional.empty()).when(usersRepository).findByEmail(users.getEmail());
        //поиск роли AUTHORITY_READ
        Authority authority = Authority.builder()
                .id(UUID.randomUUID())
                .name(EAuthority.AUTHORITY_READ)
                .build();
        doReturn(Optional.of(authority)).when(authorityRepository).findByName(EAuthority.AUTHORITY_READ);

        //эмулирование хеширование пароля
        String password = users.getPassword();
        String encodePassword = bCryptPasswordEncoder.encode(password);
        doReturn(encodePassword).when(passwordEncoder).encode(password);
        users.setPassword(encodePassword);

        //сохранения пользователя
        Users returnUsers = Users.builder()
                .id(UUID.randomUUID())
                .password(users.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();
        doReturn(returnUsers).when(usersRepository).save(users);

        UsersDto returnFromMethod = UsersDto.builder()
                .id(returnUsers.getId())
                .login(usersDto.getLogin())
                .firstName(usersDto.getFirstName())
                .lastName(usersDto.getLastName())
                .email(usersDto.getEmail())
                .build();
        //when
        UsersDto result = authorizationService.signup(usersDto);
        //then
        assertEquals(result, returnFromMethod);
        verify(usersRepository).findByLogin(users.getLogin());
        verify(usersRepository).findByEmail(users.getEmail());
        verify(usersRepository).save(users);
        verify(authorityRepository).findByName(EAuthority.AUTHORITY_READ);
        verify(passwordEncoder).encode(password);
        verifyNoMoreInteractions(usersRepository, passwordEncoder, authorityRepository);
    }

    @Test
    @DisplayName("Тест создания пользователя. Сценарий при совпадении логинов")
    void signup_ExecuteIsInvalid_ThrowUsersExistExceptionByLogin() {
        //given
        UsersDto usersDto = UsersDto.builder()
                .login("kamegatze")
                .email("aleksi.aleksi2014@yandex.ru")
                .firstName("Aleksey")
                .lastName("Shirayev")
                .password("fgrgdddsdvbhgvbbfgrewert")
                .build();
        Users users = Users.builder()
                .password(usersDto.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();
        Optional<Users> usersReturnByLogin = Optional.of(Users.builder()
                .id(UUID.randomUUID())
                .login("kamegatze")
                .email("aleksi24129958@gmail.com")
                .name("Ivan Romanov")
                .build());

        doReturn(usersReturnByLogin).when(usersRepository).findByLogin(users.getLogin());

        //when
        UsersExistException exception = assertThrows(UsersExistException.class, () -> {
            UsersDto result = authorizationService.signup(usersDto);
        });
        //then
        assertEquals(String.format("User with login: \"%s\" exist", users.getLogin()), exception.getMessage());
        verify(usersRepository).findByLogin(users.getLogin());
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Тест создания пользователя. Сценарий при совпадении email")
    void signup_ExecuteIsInvalid_ThrowUsersExistExceptionByEmail() {
        //given
        UsersDto usersDto = UsersDto.builder()
                .login("kamegatze")
                .email("aleksi.aleksi2014@yandex.ru")
                .firstName("Aleksey")
                .lastName("Shirayev")
                .password("fgrgdddsdvbhgvbbfgrewert")
                .build();
        Users users = Users.builder()
                .password(usersDto.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();
        Optional<Users> usersReturnByEmail= Optional.of(Users.builder()
                .id(UUID.randomUUID())
                .login("aleksi")
                .email("aleksi.aleksi2014@yandex.ru")
                .name("Ivan Romanov")
                .build());

        doReturn(Optional.empty()).when(usersRepository).findByLogin(users.getLogin());
        doReturn(usersReturnByEmail).when(usersRepository).findByEmail(users.getEmail());
        //when
        UsersExistException exception = assertThrows(UsersExistException.class, () -> {
            UsersDto result = authorizationService.signup(usersDto);
        });
        //then
        assertEquals(String.format("User with email: \"%s\" exist", users.getEmail()), exception.getMessage());
        verify(usersRepository).findByLogin(users.getLogin());
        verify(usersRepository).findByEmail(users.getEmail());
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Аунтефикация и авторизация пользователя в системе")
    void signin_ExecuteIsValid_AuthenticationUser() {
        //given
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        Login login = Login.builder()
                .login("kamegatze")
                .password("fgrgdddsdvbhgvbbfgrewert")
                .build();

        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(bCryptPasswordEncoder.encode(login.getPassword()))
                .authorities(
                        List.of(Authority.builder()
                        .id(UUID.randomUUID())
                        .name(EAuthority.AUTHORITY_READ).build())
                )
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UsersDetails(users),
                bCryptPasswordEncoder.encode(
                        login.getPassword()
                )
        );
        doReturn(
                authentication
        ).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        doReturn(accessToken).when(jwtService).generateAccess((UsersDetails)authentication.getPrincipal());
        doReturn(refreshToken).when(jwtService).generateRefresh((UsersDetails)authentication.getPrincipal());
        JwtDto returnResult = JwtDto.builder()
                .refreshToken(refreshToken)
                .tokenAccess(accessToken)
                .type(ETokenType.Bearer)
                .build();
        //when
        JwtDto result = authorizationService.signin(login);
        //then
        assertEquals(result, returnResult);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccess((UsersDetails)authentication.getPrincipal());
        verify(jwtService).generateRefresh((UsersDetails)authentication.getPrincipal());
        verifyNoMoreInteractions(authenticationManager, jwtService);
    }

    @Test
    @DisplayName("Проверка авторизации пользователя при положительном сценарии")
    void isAuthenticationUser_ExecuteIsValid_ReturnsTrue() throws ParseException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        Instant expired = Instant.now().plus(Period.ofDays(2));
        Instant issueAt = Instant.parse("2024-03-23T10:20:54.00Z");

        doReturn(Instant.parse("2024-03-23T10:20:54.00Z")).when(jwtService).getIssuedAt(refreshToken);
        doReturn(Instant.now().plus(Period.ofDays(2))).when(jwtService).getExpiresAt(refreshToken);
        doReturn(Map.of("alg","RS256")).when(jwtService).getHeaders(refreshToken);
        doReturn(Map.of(
                "iss", "authorization-file-manager",
                "sub", "kamegatze",
                "exp", expired.getNano(),
                "iat", issueAt.getNano(),
                "authority", "AUTHORITY_READ "
        )).when(jwtService).getClaims(refreshToken);
        doReturn(OAuth2TokenValidatorResult.success()).when(jwtValidator).validate(any(Jwt.class));
        //when
        Boolean result = authorizationService.isAuthenticationUser(request);
        //then
        assertEquals(result, Boolean.TRUE);
        verify(jwtService).getExpiresAt(refreshToken);
        verify(jwtService).getIssuedAt(refreshToken);
        verify(jwtService).getHeaders(refreshToken);
        verify(jwtService).getClaims(refreshToken);
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    @DisplayName("Проверка авторизации пользователя при положительном сценарии, если все токены пусты")
    void isAuthenticationUser_CheckTokensEmpty_ReturnsFalse() throws ParseException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        //when
        Boolean result = authorizationService.isAuthenticationUser(request);
        //then
        assertEquals(result, Boolean.FALSE);
    }

    @Test
    @DisplayName("Проверка авторизации пользователя при положительном сценарии, если есть ошибка валидация токенов")
    void isAuthentication_CheckHasErrors_ReturnsFalse() throws ParseException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        Instant expired = Instant.now().plus(Period.ofDays(2));
        Instant issueAt = Instant.parse("2024-03-23T10:20:54.00Z");

        doReturn(Instant.parse("2024-03-23T10:20:54.00Z")).when(jwtService).getIssuedAt(refreshToken);
        doReturn(Instant.parse("2024-03-25T10:20:54.00Z")).when(jwtService).getExpiresAt(refreshToken);
        doReturn(Map.of("alg","RS256")).when(jwtService).getHeaders(refreshToken);
        doReturn(Map.of(
                "iss", "authorization-file-manager",
                "sub", "kamegatze",
                "exp", expired.getNano(),
                "iat", issueAt.getNano(),
                "authority", "AUTHORITY_READ "
        )).when(jwtService).getClaims(refreshToken);
        doReturn(OAuth2TokenValidatorResult.failure(
                List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,"Time token expired",
                        "https://tools.ietf.org/html/rfc6750#section-3.1")
                ))).when(jwtValidator).validate(any(Jwt.class));
        // when
        Boolean result = authorizationService.isAuthenticationUser(request);
        // then
        assertEquals(result, Boolean.FALSE);
        verify(jwtService).getExpiresAt(refreshToken);
        verify(jwtService).getIssuedAt(refreshToken);
        verify(jwtService).getHeaders(refreshToken);
        verify(jwtService).getClaims(refreshToken);
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    @DisplayName("Проверка авторизации пользователя при положительном сценарии, если есть ошибка парсинга токена")
    void isAuthentication_CatchJwtValidationException_ReturnsFalse() throws ParseException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);

        doThrow(JwtValidationException.class).when(jwtService).getIssuedAt(refreshToken);
        //when
        Boolean result = authorizationService.isAuthenticationUser(request);
        // then

        assertEquals(result, Boolean.FALSE);
    }

    @Test
    @DisplayName("Авторизация через refresh token, положительный сценарий")
    void authenticationViaRefreshToken_ExecuteIsValid_ReturnsJwtDto() throws ParseException, RefreshTokenIsNullException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        Instant expired = Instant.now().plus(Period.ofDays(2));
        Instant issueAt = Instant.parse("2024-03-23T10:20:54.00Z");
        String login = "kamegatze";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Users users = Users.builder()
                        .id(UUID.randomUUID())
                        .name("Aleksey Shirayev")
                        .email("aleksi.aleksi2014@yandex.ru")
                        .login("kamegatze")
                        .password(encoder.encode("fgrgdddsdvbhgvbbfgrewert"))
                        .build();
        users.setAuthorities(List.of(new Authority(UUID.randomUUID(), EAuthority.AUTHORITY_READ, List.of(users))));
        UserDetails userDetails = new UsersDetails(
                users
        );

        doReturn(Instant.parse("2024-03-23T10:20:54.00Z")).when(jwtService).getIssuedAt(refreshToken);
        doReturn(Instant.now().plus(Period.ofDays(2))).when(jwtService).getExpiresAt(refreshToken);
        doReturn(Map.of("alg","RS256")).when(jwtService).getHeaders(refreshToken);
        doReturn(Map.of(
                "iss", "authorization-file-manager",
                "sub", login,
                "exp", expired.getNano(),
                "iat", issueAt.getNano(),
                "authority", "AUTHORITY_READ "
        )).when(jwtService).getClaims(refreshToken);
        doReturn(OAuth2TokenValidatorResult.success()).when(jwtValidator).validate(any(Jwt.class));
        doReturn(login).when(jwtService).getLogin(refreshToken);
        doReturn(Boolean.TRUE).when(usersRepository).existsByLogin(login);
        doReturn(userDetails).when(usersDetailsService).loadUserByUsername(login);
        doReturn(accessToken).when(jwtService).generateAccess(userDetails);
        doReturn(refreshToken).when(jwtService).generateRefresh(userDetails);
        //when
        JwtDto jwtDto = authorizationService.authenticationViaRefreshToken(request);
        //then
        JwtDto jwtThen = JwtDto.builder()
                .type(ETokenType.Bearer)
                .refreshToken(refreshToken)
                .tokenAccess(accessToken)
                .build();
        assertEquals(jwtDto, jwtThen);
        verify(jwtService).getExpiresAt(refreshToken);
        verify(jwtService).getIssuedAt(refreshToken);
        verify(jwtService).getHeaders(refreshToken);
        verify(jwtService).getClaims(refreshToken);
        verify(jwtValidator).validate(any(Jwt.class));
        verify(jwtService).getLogin(refreshToken);
        verify(usersRepository).existsByLogin(login);
        verify(usersDetailsService).loadUserByUsername(login);
        verify(jwtService).generateAccess(userDetails);
        verify(jwtService).generateRefresh(userDetails);
        verifyNoMoreInteractions(jwtService, jwtValidator, usersRepository, usersDetailsService);
    }

    @Test
    @DisplayName("Авторизация через refresh token, если refresh token null")
    void authenticationViaRefreshToken_ExecuteIsInvalid_ThrowRefreshTokenIsNullException() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        //when
        RefreshTokenIsNullException exception = assertThrows(RefreshTokenIsNullException.class, () -> {
            JwtDto result = authorizationService.authenticationViaRefreshToken(request);
        });
        //then
        assertEquals(exception.getMessage(), "Refresh token is null");
    }

    @Test
    @DisplayName("Авторизация через refresh token, если refresh token не валиден")
    void authenticationViaRefreshToken_ExecuteIsInvalid_ThrowInvalidBearerTokenException() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        Instant expired = Instant.now().plus(Period.ofDays(2));
        Instant issueAt = Instant.parse("2024-03-23T10:20:54.00Z");

        doReturn(Instant.parse("2024-03-23T10:20:54.00Z")).when(jwtService).getIssuedAt(refreshToken);
        doReturn(Instant.parse("2024-03-25T10:20:54.00Z")).when(jwtService).getExpiresAt(refreshToken);
        doReturn(Map.of("alg","RS256")).when(jwtService).getHeaders(refreshToken);
        doReturn(Map.of(
                "iss", "authorization-file-manager",
                "sub", "kamegatze",
                "exp", expired.getNano(),
                "iat", issueAt.getNano(),
                "authority", "AUTHORITY_READ "
        )).when(jwtService).getClaims(refreshToken);
        doReturn(OAuth2TokenValidatorResult.failure(
                List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,"Time token expired",
                        "https://tools.ietf.org/html/rfc6750#section-3.1")
                ))).when(jwtValidator).validate(any(Jwt.class));
        //when
        InvalidBearerTokenException exception = assertThrows(InvalidBearerTokenException.class, () -> {
            JwtDto result = authorizationService.authenticationViaRefreshToken(request);
        });
        //then
        assertEquals(exception.getMessage(), "Refresh token invalid");
        verify(jwtService).getExpiresAt(refreshToken);
        verify(jwtService).getIssuedAt(refreshToken);
        verify(jwtService).getHeaders(refreshToken);
        verify(jwtService).getClaims(refreshToken);
        verify(jwtValidator).validate(any(Jwt.class));
        verifyNoMoreInteractions(jwtService, jwtValidator);
    }

    @Test
    @DisplayName("Авторизация через refresh token, если пользователя нет в базе данных")
    void authenticationViaRefreshToken_ExecuteIsInvalid_ThrowUserNotExistException() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        Instant expired = Instant.now().plus(Period.ofDays(2));
        Instant issueAt = Instant.parse("2024-03-23T10:20:54.00Z");
        String login = "kamegatze";

        doReturn(Instant.parse("2024-03-23T10:20:54.00Z")).when(jwtService).getIssuedAt(refreshToken);
        doReturn(expired).when(jwtService).getExpiresAt(refreshToken);
        doReturn(Map.of("alg","RS256")).when(jwtService).getHeaders(refreshToken);
        doReturn(Map.of(
                "iss", "authorization-file-manager",
                "sub", "kamegatze",
                "exp", expired.getNano(),
                "iat", issueAt.getNano(),
                "authority", "AUTHORITY_READ "
        )).when(jwtService).getClaims(refreshToken);
        doReturn(OAuth2TokenValidatorResult.success()).when(jwtValidator).validate(any(Jwt.class));
        doReturn(login).when(jwtService).getLogin(refreshToken);
        doReturn(Boolean.FALSE).when(usersRepository).existsByLogin(login);
        //when
        UserNotExistException exception = assertThrows(UserNotExistException.class, () -> {
            JwtDto result = authorizationService.authenticationViaRefreshToken(request);
        });
        //then
        assertEquals(exception.getMessage(), String.format("User with login: [%s] not exist", login));
        verify(jwtService).getExpiresAt(refreshToken);
        verify(jwtService).getIssuedAt(refreshToken);
        verify(jwtService).getHeaders(refreshToken);
        verify(jwtService).getClaims(refreshToken);
        verify(jwtValidator).validate(any(Jwt.class));
        verify(jwtService).getLogin(refreshToken);
        verify(usersRepository).existsByLogin(login);
        verifyNoMoreInteractions(jwtService, jwtValidator, usersRepository);
    }

    @Test
    @DisplayName("Проверка существует ли пользователь, по логину если пользователь существует")
    void isExistUser_ExecuteIsValid_ReturnsTrueByLogin() {
        //given
        String login = "kamegatze";
        doReturn(Boolean.TRUE).when(usersRepository).existsByLogin(login);
        //when
        Boolean result = authorizationService.isExistUser(login);
        //then
        assertEquals(result, Boolean.TRUE);
        verify(usersRepository).existsByLogin(login);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Проверка существует ли пользователь, по email если пользователь существует")
    void isExistUser_ExecuteIsValid_ReturnsTrueByEmail() {
        //given
        String email = "aleksi.aleksi2014@yandex.ru";
        doReturn(Boolean.TRUE).when(usersRepository).existsByEmail(email);
        //when
        Boolean result = authorizationService.isExistUser(email);
        //then
        assertEquals(result, Boolean.TRUE);
        verify(usersRepository).existsByEmail(email);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Проверка существует ли пользователь, по логину если пользователь не существует")
    void isExistUser_ExecuteIsValid_ReturnsFalseByLogin() {
        //given
        String login = "kfgdfgdgd";
        doReturn(Boolean.FALSE).when(usersRepository).existsByLogin(login);
        //when
        Boolean result = authorizationService.isExistUser(login);
        //then
        assertEquals(result, Boolean.FALSE);
        verify(usersRepository).existsByLogin(login);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Проверка существует ли пользователь, по email если пользователь существует")
    void isExistUser_ExecuteIsValid_ReturnsFalseByEmail() {
        //given
        String email = "aleksi.aleksi2014@yandex.ru";
        doReturn(Boolean.FALSE).when(usersRepository).existsByEmail(email);
        //when
        Boolean result = authorizationService.isExistUser(email);
        //then
        assertEquals(result, Boolean.FALSE);
        verify(usersRepository).existsByEmail(email);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Отправка ссылки для смены пароля, для поиска пользователя по логину")
    void sendCode_ExecuteIsValid_GenerateLinkUsersByEmail() throws MessagingException {
        //given
        String email = "aleksi.aleksi2014@yandex.ru";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(encoder.encode("fgsdfsgfdfggdgfsgd"))
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .build();
        String htmlBody = "<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\" lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "    <title>Recovery code</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Нажмите на <a th:href=\"${link}\">ссылку</a> для перехода на страницу смена пароля</p>\n" +
                "</body>\n" +
                "</html>";

        doReturn(Optional.of(users)).when(usersRepository).findByEmail(email);
        users.setRecoveryCode(UUID.randomUUID().toString());
        doReturn(users).when(usersRepository).save(users);
        doReturn(htmlBody).when(templateEngine).process(anyString(), any(Context.class));
        doNothing().when(emailService).sendHtmlMessage(eq(users.getEmail()), anyString(), eq(htmlBody));
        //when
        authorizationService.sendCode(email);
        //then
        verify(usersRepository).findByEmail(email);
        verify(usersRepository).save(users);
        verify(templateEngine).process(anyString(), any(Context.class));
        verify(emailService).sendHtmlMessage(eq(users.getEmail()), anyString(), eq(htmlBody));
        verifyNoMoreInteractions(usersRepository, templateEngine, emailService);
    }

    @Test
    @DisplayName("Отправка ссылки для смены пароля, для поиска пользователя по email")
    void sendCode_ExecuteIsValid_GenerateLinkUsersByLogin() throws MessagingException {
        //given
        String login = "kamegatze";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(encoder.encode("fgsdfsgfdfggdgfsgd"))
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .build();
        String htmlBody = "<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\" lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "    <title>Recovery code</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Нажмите на <a th:href=\"${link}\">ссылку</a> для перехода на страницу смена пароля</p>\n" +
                "</body>\n" +
                "</html>";

        doReturn(Optional.of(users)).when(usersRepository).findByLogin(login);
        users.setRecoveryCode(UUID.randomUUID().toString());
        doReturn(users).when(usersRepository).save(users);
        doReturn(htmlBody).when(templateEngine).process(anyString(), any(Context.class));
        doNothing().when(emailService).sendHtmlMessage(eq(users.getEmail()), anyString(), eq(htmlBody));
        //when
        authorizationService.sendCode(login);
        //then
        verify(usersRepository).findByLogin(login);
        verify(usersRepository).save(users);
        verify(templateEngine).process(anyString(), any(Context.class));
        verify(emailService).sendHtmlMessage(eq(users.getEmail()), anyString(), eq(htmlBody));
        verifyNoMoreInteractions(usersRepository, templateEngine, emailService);
    }

    @Test
    @DisplayName("Отправка ссылки для смены пароля, по логину. Обработки исключения пользоватеь не найден")
    void sendCode_ExecuteIsInvalid_ThrowsUserNotExistExceptionByLogin() {
        //given
        String login = "kamegatze";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(encoder.encode("fgsdfsgfdfggdgfsgd"))
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .build();
        String htmlBody = "<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\" lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "    <title>Recovery code</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Нажмите на <a th:href=\"${link}\">ссылку</a> для перехода на страницу смена пароля</p>\n" +
                "</body>\n" +
                "</html>";

        doReturn(Optional.empty()).when(usersRepository).findByLogin(login);
        //when
        UserNotExistException exception = assertThrows(UserNotExistException.class, () -> {
            authorizationService.sendCode(login);
        });
        //then
        assertEquals(exception.getMessage(), String.format("Not found user by login: \"%s\"", login));
        verify(usersRepository).findByLogin(login);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Отправка ссылки для смены пароля, по email. Обработки исключения пользоватеь не найден")
    void sendCode_ExecuteIsInvalid_ThrowsUserNotExistExceptionByEmail() {
        //given
        String email = "aleksi.aleksi2014@yandex.ru";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(encoder.encode("fgsdfsgfdfggdgfsgd"))
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .build();
        String htmlBody = "<!DOCTYPE html>\n" +
                "<html xmlns:th=\"http://www.thymeleaf.org\" lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "    <title>Recovery code</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>Нажмите на <a th:href=\"${link}\">ссылку</a> для перехода на страницу смена пароля</p>\n" +
                "</body>\n" +
                "</html>";

        doReturn(Optional.empty()).when(usersRepository).findByEmail(email);
        //when
        UserNotExistException exception = assertThrows(UserNotExistException.class, () -> {
            authorizationService.sendCode(email);
        });
        //then
        assertEquals(exception.getMessage(), String.format("Not found user by email: \"%s\"", email));
        verify(usersRepository).findByEmail(email);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Смена пароля. Метод выполняется корректно")
    void changePassword_ExecuteIsValid_UserChangePassword() throws EqualsPasswordException, NotEqualsPasswordException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //given
        UUID recoveryCode = UUID.randomUUID();

        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .recoveryCode(recoveryCode.toString())
                .password("gsdassgffgdfgsgfdsgsg")
                .passwordRetry("gsdassgffgdfgsgfdsgsg")
                .build();
        String password = encoder.encode("gsdassgffgdfgsgfdsgsg");
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(encoder.encode("sfsfdgdgdfwfqwdwfwf"))
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .recoveryCode(recoveryCode.toString())
                .build();

        doReturn(Optional.of(users)).when(usersRepository).findByRecoveryCode(users.getRecoveryCode());
        doReturn(password).when(passwordEncoder).encode(changePasswordDto.getPassword());
        doReturn(Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(password)
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .recoveryCode("")
                .build()).when(usersRepository).save(users);
        //when
        authorizationService.changePassword(changePasswordDto);
        //then
        verify(usersRepository).findByRecoveryCode(recoveryCode.toString());
        verify(passwordEncoder).encode(changePasswordDto.getPassword());
        verify(usersRepository).save(any(Users.class));
        verifyNoMoreInteractions(usersRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Смена пароля. Метод выкидывает исключение NotEqualsPasswordException")
    void sendCode_ExecuteIsInvalid_ThrowsNotEqualsPasswordException() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //given
        UUID recoveryCode = UUID.randomUUID();

        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .recoveryCode(recoveryCode.toString())
                .password("gsdassgffgdfgsgfdsgsg")
                .passwordRetry("gsdassgffgdfgsgfdsgs")
                .build();
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(encoder.encode("gsdassgffgdfgsgfdsgsg"))
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .recoveryCode(recoveryCode.toString())
                .build();

        doReturn(Optional.of(users)).when(usersRepository).findByRecoveryCode(changePasswordDto.getRecoveryCode());
        //when
        NotEqualsPasswordException exception = assertThrows(NotEqualsPasswordException.class, () -> {
           authorizationService.changePassword(changePasswordDto);
        });
        //then
        assertEquals(exception.getMessage(), "Field password and passwordRetry not equals");
        verify(usersRepository).findByRecoveryCode(recoveryCode.toString());
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    @DisplayName("Смена пароля. Метод выкидывает исключение EqualsPasswordException")
    void changePassword_ExecuteIsInvalid_ThrowsEqualsPasswordException() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //given
        UUID recoveryCode = UUID.randomUUID();

        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .recoveryCode(recoveryCode.toString())
                .password("gsdassgffgdfgsgfdsgsg")
                .passwordRetry("gsdassgffgdfgsgfdsgsg")
                .build();
        String password = encoder.encode("gsdassgffgdfgsgfdsgsg");
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login("kamegatze")
                .password(password)
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .recoveryCode(recoveryCode.toString())
                .build();

        doReturn(Optional.of(users)).when(usersRepository).findByRecoveryCode(users.getRecoveryCode());
        doReturn(password).when(passwordEncoder).encode(changePasswordDto.getPassword());
        //when
        EqualsPasswordException exception = assertThrows(EqualsPasswordException.class, () -> {
            authorizationService.changePassword(changePasswordDto);
        });
        //then
        assertEquals(exception.getMessage(), "Input other password. Current password equals previous password");
        verify(usersRepository).findByRecoveryCode(changePasswordDto.getRecoveryCode());
        verify(passwordEncoder).encode(changePasswordDto.getPassword());
        verifyNoMoreInteractions(usersRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Смена пароля. Метод выкидывает исключение NoSuchElementException")
    void changePassword_ExecuteIsInvalid_ThrowsNoSuchElementException() {
        //given
        UUID recoveryCode = UUID.randomUUID();
        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .recoveryCode(recoveryCode.toString())
                .password("gsdassgffgdfgsgfdsgsg")
                .passwordRetry("gsdassgffgdfgsgfdsgsg")
                .build();

        doReturn(Optional.empty()).when(usersRepository).findByRecoveryCode(changePasswordDto.getRecoveryCode());
        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
			authorizationService.changePassword(changePasswordDto);
        });
        //then
		assertEquals(exception.getMessage(), String.format("User not found by recovery code: \"%s\"", changePasswordDto.getRecoveryCode()));
		verify(usersRepository).findByRecoveryCode(changePasswordDto.getRecoveryCode());
		verifyNoMoreInteractions(usersRepository);
	}

	@Test
	@DisplayName("Получние прав для другого сервиса. Возвращение пустого списка")
	void getAuthorityByRequest_ExecuteIsValid_ReturnsEmptyList() throws ParseException {
		//given
		MockHttpServletRequest request = new MockHttpServletRequest();
		//when
		List<AuthorityDto> result = authorizationService.getAuthorityByRequest(request);
		//then
		assertEquals(result.size(), 0);
	}

	@Test
	@DisplayName("Получние прав для другого сервиса. Если пользователь не найден")
	void getAuthorityByRequest_ExecuteIsInvalid_ThrowsUserNotExistException() throws ParseException {
		//given
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
		JWTClaimsSet claimsSet = JWTParser.parse(accessToken).getJWTClaimsSet();
		String login = claimsSet.getSubject();

		doReturn(Optional.empty()).when(usersRepository).findByLogin(login);
		//when
		UserNotExistException exception = assertThrows(UserNotExistException.class, () -> {
			List<AuthorityDto> result = authorizationService.getAuthorityByRequest(request);
		});
		//then
		assertEquals(exception.getMessage(), String.format("User with login: [%s] not exist", claimsSet.getSubject()));
		verify(usersRepository).findByLogin(login);
		verifyNoMoreInteractions(usersRepository);
	}

	@Test
	@DisplayName("Получние прав для другого сервиса. Удачный кейс")
	void getAuthorityByRequest_ExecuteIsValid_ReturnsAuthorityList() throws ParseException {
		//given
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
		JWTClaimsSet claimsSet = JWTParser.parse(accessToken).getJWTClaimsSet();
		String login = claimsSet.getSubject();
		String password = encoder.encode("gsdassgffgdfgsgfdsgsg");
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .name("Aleksey Shirayev")
                .email("aleksi.aleksi2014@yandex.ru")
                .login(login)
                .password(password)
                .authorities(
                        List.of(Authority.builder()
                                .id(UUID.randomUUID())
                                .name(EAuthority.AUTHORITY_READ).build())
                )
                .build();

		doReturn(Optional.of(users)).when(usersRepository).findByLogin(login);
		//when
		List<AuthorityDto> result = authorizationService.getAuthorityByRequest(request);
		//then
		List<AuthorityDto> authority = users.getAuthorities().stream()
			.map(item -> new AuthorityDto(item.getName().name()))
			.toList();

		assertEquals(result, authority);
		verify(usersRepository).findByLogin(login);
		verifyNoMoreInteractions(usersRepository);
	}
}