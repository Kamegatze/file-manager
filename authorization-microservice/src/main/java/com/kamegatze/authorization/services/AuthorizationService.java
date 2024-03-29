package com.kamegatze.authorization.services;

import com.kamegatze.authorization.dto.*;
import com.kamegatze.authorization.exception.EqualsPasswordException;
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
import java.util.List;

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
                  String loginOrEmail) throws MessagingException;

    void changePassword(@Valid ChangePasswordDto changePasswordDto) throws NotEqualsPasswordException, EqualsPasswordException;

    List<AuthorityDto> getAuthorityByRequest(HttpServletRequest request) throws ParseException;
}
