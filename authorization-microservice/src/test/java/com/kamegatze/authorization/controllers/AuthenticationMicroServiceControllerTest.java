package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.AuthorityDto;
import com.kamegatze.authorization.dto.ETypeTokenHeader;
import com.kamegatze.authorization.model.EAuthority;
import com.kamegatze.authorization.services.AuthorizationService;
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

import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тест контроллера для авторизации других сервисов")
class AuthenticationMicroServiceControllerTest {
    @Mock
    AuthorizationService authorizationService;
    @InjectMocks
    AuthenticationMicroServiceController authenticationMicroServiceController;
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
    }

    @Test
    @DisplayName("Проверка авторизован клиет или нет")
    void handleIsAuthentication_RequestIsValid_ReturnsResponseListAuthority() throws ParseException {
        //given
        List<AuthorityDto> authorityDtos = List.of(AuthorityDto.builder()
                .authority(EAuthority.AUTHORITY_READ.name()).build(), AuthorityDto.builder()
                        .authority(EAuthority.AUTHORITY_WRITE.name())
                        .build()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ETypeTokenHeader.Authorization.name(), accessToken);
        request.addHeader(ETypeTokenHeader.AuthorizationRefresh.name(), refreshToken);
        doReturn(authorityDtos).when(authorizationService).getAuthorityByRequest(request);
        //when
        ResponseEntity<List<AuthorityDto>> result = authenticationMicroServiceController
                .handleIsAuthentication(request);
        //then
        ResponseEntity<List<AuthorityDto>> response = ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(authorityDtos);
        assertEquals(response, result);
        verify(authorizationService).getAuthorityByRequest(request);
        verifyNoMoreInteractions(authorizationService);
    }
}