package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.ChangePasswordDto;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.EqualsPasswordException;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.general.dto.response.ResponseDto;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.text.ParseException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/service")
public class AuthenticationController {

    private final AuthorizationService authorizationService;
    @PostMapping("/signup")
    public ResponseEntity<ResponseDto> handleSignUpUser(@RequestBody UsersDto usersDto, UriComponentsBuilder uri)
            throws UsersExistException {
        UsersDto usersSave = authorizationService.signup(usersDto);
        ResponseDto response = ResponseDto.builder()
                .message("User was created")
                .status(HttpStatus.CREATED)
                .build();
        return ResponseEntity.created(uri.path("/api/auth/info/user/{id}")
                .build(Map.of("id", usersSave.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtDto> handleSignInUser(@RequestBody Login login) {
        JwtDto jwtDto = authorizationService.signin(login);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jwtDto);
    }

    @GetMapping("/is-authentication")
    public ResponseEntity<Boolean> handleIsAuthenticationUser(HttpServletRequest request) throws ParseException {
        Boolean isAuthentication = authorizationService.isAuthenticationUser(request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(isAuthentication);
    }

    @GetMapping("/authentication")
    public ResponseEntity<JwtDto> handleAuthenticationUserUseRefreshToken(HttpServletRequest request)
            throws InvalidBearerTokenException, ParseException, RefreshTokenIsNullException, UserNotExistException {
        JwtDto jwtDto = authorizationService.authenticationViaRefreshToken(request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jwtDto);
    }

    @PostMapping("/send-email-change-password")
    public ResponseEntity<ResponseDto> handleSendEmailChangePassword(@RequestParam String loginOrEmail)
            throws MessagingException {
        authorizationService.sendCode(loginOrEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .status(HttpStatus.OK)
                        .message("Go to your mailbox to recover " +
                                "your password")
                        .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseDto> handleChangePassword(@RequestBody ChangePasswordDto changePasswordDto)
            throws NotEqualsPasswordException, EqualsPasswordException {
        authorizationService.changePassword(changePasswordDto);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.builder()
                        .message("Your password change")
                        .status(HttpStatus.OK)
                        .build());
    }
}
