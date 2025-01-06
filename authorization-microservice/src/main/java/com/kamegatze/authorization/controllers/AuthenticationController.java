package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.InfoAboutUser;
import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.general.dto.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/auth/service")
@Tag(name = "Authentication", description = "Work with authentication user in system")
public class AuthenticationController {

    private final AuthorizationService authorizationService;

    @Operation(
            summary = "Info user",
            description = "Info user about type authorization"
    )
    @GetMapping("/info-user-by-login")
    public ResponseEntity<InfoAboutUser> handleExistUserByLogin(@RequestParam(value = "login", required = true)
                                                           @Parameter(description = "login user", name = "login",
                                                                   example = "kamegatze", required = true)
                                                                    String login) {
        InfoAboutUser infoAboutUser = authorizationService.getInfoAboutUserByLogin(login);
        return ResponseEntity.ok(infoAboutUser);
    }

    @Operation(
            summary = "Registration user in system",
            description = "Registration user in system after send user in kafka for addition in other microservice"
    )
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

    @Operation(
            summary = "Sign in in system",
            description = "Sign in system and get jwt tokens"
    )
    @PostMapping("/signin")
    public ResponseEntity<JwtDto> handleSignInUser(@RequestBody Login login) {
        JwtDto jwtDto = authorizationService.signin(login);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jwtDto);
    }

    @Operation(
            summary = "Check user is authentication via refresh token",
            description = "Check user is authentication via refresh token. Check on empty access, refresh token and validation refresh token. In headers must be token access and refresh"
    )
    @GetMapping("/is-authentication")
    public ResponseEntity<Boolean> handleIsAuthenticationUser(HttpServletRequest request) throws ParseException {
        Boolean isAuthentication = authorizationService.isAuthenticationUser(request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(isAuthentication);
    }
    
    @GetMapping("/is-authentication-via-response-code")
    public ResponseEntity<Void> handleIsAuthenticationViaResponseCode(HttpServletRequest request) throws ParseException {
        var isAuthentication = authorizationService.isAuthenticationUser(request);
        return isAuthentication ? ResponseEntity.status(HttpStatus.OK).build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(
            summary = "Authorization via refresh token",
            description = "Authorization via refresh token, for update access and refresh token. In headers must be refresh token"
    )
    @GetMapping("/authentication")
    public ResponseEntity<JwtDto> handleAuthenticationUserUseRefreshToken(HttpServletRequest request)
            throws InvalidBearerTokenException, ParseException, RefreshTokenIsNullException, UserNotExistException {
        JwtDto jwtDto = authorizationService.authenticationViaRefreshToken(request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jwtDto);
    }
}
