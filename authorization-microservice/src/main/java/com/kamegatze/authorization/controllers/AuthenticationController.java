package com.kamegatze.authorization.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamegatze.authorization.dto.ChangePasswordDto;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.Response;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.service.AuthorizationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/service")
public class AuthenticationController {

    private final AuthorizationService authorizationService;
    @PostMapping("/signup")
    public ResponseEntity<Response> handleSignUpUser(@RequestBody UsersDto usersDto, UriComponentsBuilder uri) throws UsersExistException {

        UsersDto usersSave = authorizationService.signup(usersDto);

        Response response = Response.builder()
                .message("User was created")
                .returnCode(202)
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
    public void handleAuthenticationUserUseRefreshToken(HttpServletRequest request, HttpServletResponse response)
            throws InvalidBearerTokenException, ParseException, RefreshTokenIsNullException, UserNotExistException, IOException {
        JwtDto jwtDto = authorizationService.authenticationViaRefreshToken(request);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(
                response.getOutputStream(), jwtDto);
    }

    @PostMapping("/send-email-change-password")
    public ResponseEntity<Response> handleSendEmailChangePassword(@RequestParam String loginOrEmail)
            throws ExecutionException, InterruptedException, MessagingException {
        authorizationService.sendCode(loginOrEmail);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .returnCode(200)
                        .message("Go to your mailbox to recover " +
                                "your password")
                        .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Response> handleChangePassword(@RequestBody ChangePasswordDto changePasswordDto) throws ExecutionException, InterruptedException, NotEqualsPasswordException {
        authorizationService.changePassword(changePasswordDto);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Response.builder()
                        .message("Your password change")
                        .returnCode(200)
                        .build());
    }
}
