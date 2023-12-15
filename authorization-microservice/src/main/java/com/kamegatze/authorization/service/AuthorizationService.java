package com.kamegatze.authorization.service;

import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.UsersExistException;

public interface AuthorizationService {
    public UsersDto signup(UsersDto usersDto) throws UsersExistException;

    public JwtDto signin(Login login);
}
