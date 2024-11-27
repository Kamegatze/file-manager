package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.model.Authority;
import com.kamegatze.authorization.model.EAuthority;
import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class AuthenticationControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;


    @Test
    @Sql("/sql/test-user.sql")
    void handleSignInUser_IsValidRequest_ReturnsResponseJwtDto() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/auth/service/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "kamegatze",
                        "password": "1234567890"
                    }
                """);

        //when
        MvcResult result = this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.tokenAccess").exists(),
                        jsonPath("$.tokenAccess").isString(),
                        jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshToken").isString(),
                        jsonPath("$.type").exists(),
                        jsonPath("$.type").isString()
                )
                .andReturn();
    }

    @Test
    void handleSignInUser_IsInvalidRequest_ReturnsResponseDto() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/auth/service/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .locale(new Locale("ru", "RU"))
                .content("""
                    {
                        "login": "kamegatze",
                        "password": "1234567890"
                    }
                """);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.message").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.status").exists(),
                        jsonPath("$.status").isString()
                );
    }

    @Test
    void handleSignUpUser_RequestIsValid_ReturnsResponseDto() throws Exception {

        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/auth/service/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "kamegatze",
                        "password": "1234567890",
                        "firstName": "Алексей",
                        "lastName": "Ширяев",
                        "email": "aleksi.aleksi2014@yandex.ru"
                    }
                """);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isCreated(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.message").exists(),
                        jsonPath("$.status").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.status").isString()
                );
    }

    @Test
    @Sql("/sql/test-user.sql")
    void handleSignUpUser_RequestIsInvalid_ReturnsResponseDto() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/auth/service/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "kamegatze",
                        "password": "1234567890",
                        "firstName": "Алексей",
                        "lastName": "Ширяев",
                        "email": "aleksi.aleksi2014@yandex.ru"
                    }
                """);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.message").exists(),
                        jsonPath("$.status").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.status").isString()
                );
    }

    @Test
    void handleIsAuthenticationUser_RequestIsValid_ReturnsTrue() throws Exception {
        //given
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .login("kamegatze")
                .password("1234567890")
                .build();

        List<Authority> authorities = List.of(
                new Authority(
                        UUID.fromString("3d777092-9793-11ee-b9d1-0242ac120002"),
                        EAuthority.AUTHORITY_READ,
                        List.of(users)
                )
        );
        users.setAuthorities(authorities);

        String accessToken = jwtService.generateAccess(new UsersDetails(users));
        String refreshToken = jwtService.generateRefresh(new UsersDetails(users));
        var requestBuilder = MockMvcRequestBuilders.get("/api/auth/service/is-authentication")
                .header(ETypeTokenHeader.Authorization.name(), accessToken)
                .header(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    @Sql("/sql/test-user.sql")
    void handleAuthenticationUserUseRefreshToken_RequestIsValid_ReturnsJwtDto() throws Exception {
        //given
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .login("kamegatze")
                .password("1234567890")
                .build();

        List<Authority> authorities = List.of(
                new Authority(
                        UUID.fromString("3d777092-9793-11ee-b9d1-0242ac120002"),
                        EAuthority.AUTHORITY_READ,
                        List.of(users)
                )
        );
        users.setAuthorities(authorities);

        String refreshToken = jwtService.generateRefresh(new UsersDetails(users));
        var requestBuilder = MockMvcRequestBuilders.get("/api/auth/service/authentication")
                .header(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);

        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.tokenAccess").exists(),
                        jsonPath("$.tokenAccess").isString(),
                        jsonPath("$.refreshToken").exists(),
                        jsonPath("$.refreshToken").isString(),
                        jsonPath("$.type").exists(),
                        jsonPath("$.type").isString()
                );
    }

    @Test
    @Sql("/sql/test-user.sql")
    void handleAuthenticationUserUseRefreshToken_RequestIsInvalid_ReturnsResponseDtoByThrowJwtException() throws Exception {
        //given
        Users users = Users.builder()
                .id(UUID.randomUUID())
                .login("kamegatze")
                .password("1234567890")
                .build();

        List<Authority> authorities = List.of(
                new Authority(
                        UUID.fromString("3d777092-9793-11ee-b9d1-0242ac120002"),
                        EAuthority.AUTHORITY_READ,
                        List.of(users)
                )
        );
        users.setAuthorities(authorities);

        String refreshToken = jwtService.generateRefresh(new UsersDetails(users));
        refreshToken = refreshToken.substring(0, refreshToken.length() - 10);

        var requestBuilder = MockMvcRequestBuilders.get("/api/auth/service/authentication")
                .header(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.message").exists(),
                        jsonPath("$.status").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.status").isString()
                );
    }

    @Test
    @Sql("/sql/test-user.sql")
    void handleSendEmailChangePassword_RequestIsValid_ReturnsResponseDto() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/auth/service/send-email-change-password")
                .queryParam("loginOrEmail", "kamegatze")
                .locale(Locale.ENGLISH);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                            {
                                "message": "Go to your mailbox to recover your password"
                            }
                        """),
                        jsonPath("$.message").exists(),
                        jsonPath("$.status").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.status").isString()
                );
    }

    @Test
    @Sql("/sql/test-user-with-change-code.sql")
    void handleChangePassword_RequestIsValid_ReturnsResponseDto() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/api/auth/service/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .locale(Locale.ENGLISH)
                .content("""
                    {
                        "password": "0987654321",
                        "passwordRetry": "0987654321",
                        "recoveryCode": "3f193465-ee41-41d0-a1ee-9477fdfb302f"
                    }
                """);
        //when
        mockMvc.perform(requestBuilder)
        //then
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                            {
                                "message": "Your password change"
                            }
                        """),
                        jsonPath("$.message").exists(),
                        jsonPath("$.status").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.status").isString()
                );
    }
}