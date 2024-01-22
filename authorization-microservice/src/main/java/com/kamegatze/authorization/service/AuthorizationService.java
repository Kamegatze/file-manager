package com.kamegatze.authorization.service;

import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.text.ParseException;

public interface AuthorizationService {
    public UsersDto signup(UsersDto usersDto) throws UsersExistException;

    public JwtDto signin(Login login);

    Boolean isAuthenticationUser(HttpServletRequest request) throws ParseException;

    JwtDto authenticationViaRefreshToken(HttpServletRequest request)
            throws RefreshTokenIsNullException, ParseException, InvalidBearerTokenException, UserNotExistException;

    Boolean isExistUser(String loginOrEmail);
}
