package com.kamegatze.authorization.service;

import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.UsersExistException;

import java.io.IOException;
import java.text.ParseException;

public interface AuthorizationService {
    public UsersDto signup(UsersDto usersDto) throws UsersExistException;

    public JwtDto signin(Login login);

    Boolean isAuthenticationUser(String token) throws ParseException;
}
