package com.kamegatze.authorization.service;

import com.kamegatze.authorization.dto.ChangePasswordDto;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.annotation.Validated;

import java.text.ParseException;
import java.util.concurrent.ExecutionException;
@Validated
public interface AuthorizationService {
    public UsersDto signup(@Valid UsersDto usersDto) throws UsersExistException;

    public JwtDto signin(@Valid Login login);

    Boolean isAuthenticationUser(HttpServletRequest request) throws ParseException;

    JwtDto authenticationViaRefreshToken(HttpServletRequest request)
            throws RefreshTokenIsNullException, ParseException, InvalidBearerTokenException, UserNotExistException;

    Boolean isExistUser(@NotBlank @NotEmpty @NotNull @Size(min = 5, message = "Your login or email need more 5 sign")
                        String loginOrEmail);

    void sendCode(@NotBlank @NotEmpty @NotNull @Size(min = 5, message = "Your login or email need more 5 sign")
                  String loginOrEmail) throws ExecutionException, InterruptedException, MessagingException;

    void changePassword(@Valid ChangePasswordDto changePasswordDto) throws ExecutionException, InterruptedException, NotEqualsPasswordException;
}
