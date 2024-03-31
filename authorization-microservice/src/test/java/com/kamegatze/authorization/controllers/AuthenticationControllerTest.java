package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.ChangePasswordDto;
import com.kamegatze.authorization.dto.ETokenType;
import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.EqualsPasswordException;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.general.dto.response.ResponseDto;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.util.UriComponentsBuilder;


import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тест методов AuthenticationController")
class AuthenticationControllerTest {
    @Mock
    AuthorizationService authorizationService;
    @InjectMocks
    AuthenticationController authenticationController;

    PasswordEncoder passwordEncoder;

    String accessToken;
    String refreshToken;
    @BeforeEach
    void init() {
        accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRob3JpemF0aW9u" +
                "LWZpbGUtbWFuYWdlciIsInN1YiI6ImthbWVnYXR6ZSIsImV4cCI6MTcxMTA0NzU0" +
                "OSwiaWF0IjoxNzExMDQ1NzQ5LCJhdXRob3JpdHkiOiJBVVRIT1JJVFlfUkVBRCAifQ." +
                "tZpYYAyyKAr4lx3qlXYJ0VwqeCZ463wLhVpckfkehVW10A-JqxsJxYIwkMrgDQ772uUcO" +
                "NDNmqV98Np9UB-jzZkrH_j9Qrs92BQIqSslRo9K6mlnx3FCezGw49eJC0HFTnbYO" +
                "8G6R0MtjPtJyVXGh2E7C4ADwgo3Lu6A24Ip0GP0nNZHVEEhLljAwBc25vYk-SQY3_5aya" +
                "4V6T-CPSpI0inGWfNDZ6m6yPXeykKpEvM6ltFBdkTjQun7lA1WlDwduLPnQA_XmiGCL77" +
                "uRSPl66USucwSDS8CBokc_9gk3O8evypiXSR3B0y7iYHLbGP_ovSAQdwYVOwDiDd15cAzCw";
        refreshToken = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRob3JpemF0aW9uLWZpbGUtb" +
                "WFuYWdlciIsInN1YiI6ImthbWVnYXR6ZSIsImV4cCI6MTcxMTEzMjE0OSwiaWF0IjoxNzExM" +
                "DQ1NzQ5LCJhdXRob3JpdHkiOiJBVVRIT1JJVFlfUkVBRCAifQ.bvtwUC4I62Ioo1I3_b6lW45" +
                "nhuWA5VwU1g6ti2LMhdyV6pEYCP0mkRVFSjQP9wVOEJMQe3jyTTnLnT4ucAgdRyeSm4GeApvBe" +
                "92O7pI2daDQVX1lpdypuPdLpygB0jNrI76S8BJbjsaRpA6lEZrMW8VpCR6ZXNQO0wyfAemDSKo" +
                "PSwI0Mc3MVFpRPH5LrDf9RE7cAxHXBoAYfWNeIqvGr_qeIsIVdJle42oxYIX4r2CbEUNXMYjfqmg" +
                "98EUkrxgYxS16wh1tATkQWaMqESSokwlI4DBxOK4nCyT74UubG9RhUMrbxXzsq7ORH5hN8h8rUbj" +
                "BWQTVwm2CwWmsQP0YYQ";
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("Создает пользователя в системе и возвращает ответ о создании пользователя")
    void handleSignUpUser_RequestIsValid_ReturnsResponseAboutCreateUser() throws UsersExistException {
        //given
        UsersDto usersDto = UsersDto.builder()
                .login("kamegatze")
                .email("aleksi.aleksi2014@yandex.ru")
                .firstName("Aleksey")
                .lastName("Shirayev")
                .password("fgrgdddsdvbhgvbbfgrewert")
                .build();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        UUID id = UUID.randomUUID();
        doReturn(UsersDto.builder()
                .id(id)
                .login("kamegatze")
                .email("aleksi.aleksi2014@yandex.ru")
                .firstName("Aleksey")
                .lastName("Shirayev")
                .password(passwordEncoder.encode("fgrgdddsdvbhgvbbfgrewert"))
                .build()).when(authorizationService).signup(usersDto);
        //when
        ResponseEntity<ResponseDto> responseDtoResponseEntity = authenticationController
                .handleSignUpUser(usersDto, uriBuilder);
        //then
        uriBuilder = UriComponentsBuilder.newInstance();
        ResponseEntity<ResponseDto> responseThen = ResponseEntity
                .status(HttpStatus.CREATED)
                .location(
                        uriBuilder.path("/api/auth/info/user/{id}")
                        .build(
                                Map.of("id", id)
                        )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .message("User was created")
                        .status(HttpStatus.CREATED)
                        .build()
                );
        assertEquals(responseThen, responseDtoResponseEntity);
        verify(authorizationService).signup(usersDto);
        verifyNoMoreInteractions(authorizationService);
    }

    @Test
    @DisplayName("Вход пользователя в систему при успешной авторизации")
    void handleSignInUser_RequestIsValid_ReturnJwtDto() {
        //given
        Login login = Login.builder()
                .login("kamegatze")
                .password("fgrgdddsdvbhgvbbfgrewert")
                .build();

        doReturn(JwtDto.builder()
                .type(ETokenType.Bearer)
                .refreshToken(refreshToken)
                .tokenAccess(accessToken)
                .build()).when(authorizationService).signin(login);
        //when
        ResponseEntity<JwtDto> responseDtoResponseEntity = authenticationController.handleSignInUser(login);
        //then
        ResponseEntity<JwtDto> responseThen = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JwtDto.builder()
                        .type(ETokenType.Bearer)
                        .refreshToken(refreshToken)
                        .tokenAccess(accessToken)
                        .build());

        assertEquals(responseThen, responseDtoResponseEntity);
        verify(authorizationService).signin(login);
        verifyNoMoreInteractions(authorizationService);
    }

    @Test
    @DisplayName("Проверка есть ли jwt токены в header и валидация refresh token")
    void handleIsAuthenticationUser_RequestIsValid_ReturnsTrue() throws ParseException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), String.format("Bearer %s", accessToken));
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        doReturn(Boolean.TRUE)
                .when(authorizationService).isAuthenticationUser(request);
        //when
        ResponseEntity<Boolean> result = authenticationController.handleIsAuthenticationUser(request);
        //then
        ResponseEntity<Boolean> responseThen = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Boolean.TRUE);
        assertEquals(result, responseThen);
        verify(authorizationService).isAuthenticationUser(request);
        verifyNoMoreInteractions(authorizationService);
    }

    @Test
    @DisplayName("Аунтефикация через refresh token")
    void handleAuthenticationUserUseRefreshToken_RequestIsValid_ReturnsJwtDto() throws ParseException, RefreshTokenIsNullException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        doReturn(JwtDto.builder()
                .tokenAccess(accessToken)
                .refreshToken(refreshToken)
                .type(ETokenType.Bearer)
                .build()).when(authorizationService).authenticationViaRefreshToken(request);
        //when
        ResponseEntity<JwtDto> result = authenticationController.handleAuthenticationUserUseRefreshToken(request);
        //then
        ResponseEntity<JwtDto> responseThen = ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(JwtDto.builder()
                                        .refreshToken(refreshToken)
                                        .tokenAccess(accessToken)
                                        .type(ETokenType.Bearer)
                                        .build());
        assertEquals(result, responseThen);
        verify(authorizationService).authenticationViaRefreshToken(request);
        verifyNoMoreInteractions(authorizationService);
    }

    @Test
    @DisplayName("Начало изменения пароля и отправка на email сообщения об c url для изменея пароля")
    void handleSendEmailChangePassword_RequestIsValid_ReturnsResponseDto() throws MessagingException {
        //given
        String login = "kamegatze";
        doNothing().when(authorizationService).sendCode(login);
        //when
        ResponseEntity<ResponseDto> result = authenticationController.handleSendEmailChangePassword(login);
        //then
        ResponseEntity<ResponseDto> response = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.OK)
                        .message("Go to your mailbox to recover " +
                                "your password")
                        .build());
        assertEquals(response, result);
        verify(authorizationService).sendCode(login);
        verifyNoMoreInteractions(authorizationService);
    }

    @Test
    @DisplayName("Изменение пароля пользователя")
    void handleChangePassword_RequestIsValid_ReturnsResponseDto()
            throws EqualsPasswordException, NotEqualsPasswordException {
        //given
        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .recoveryCode(UUID.randomUUID().toString())
                .password("fgrgdddsdvbhgvbbfgrewert")
                .passwordRetry("fgrgdddsdvbhgvbbfgrewert")
                .build();
        doNothing().when(authorizationService).changePassword(changePasswordDto);
        //when
        ResponseEntity<ResponseDto> result = authenticationController.handleChangePassword(changePasswordDto);
        //then
        ResponseEntity<ResponseDto> responseDto = ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .message("Your password change")
                        .status(HttpStatus.OK)
                        .build());
        assertEquals(result, responseDto);
        verify(authorizationService).changePassword(changePasswordDto);
        verifyNoMoreInteractions(authorizationService);
    }
}