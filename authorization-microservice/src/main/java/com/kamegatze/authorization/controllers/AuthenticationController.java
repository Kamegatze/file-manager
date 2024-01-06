package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.JwtDto;
import com.kamegatze.authorization.dto.Login;
import com.kamegatze.authorization.dto.Response;
import com.kamegatze.authorization.dto.UsersDto;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/authentication")
    public ResponseEntity<Boolean> handleIsAuthenticationUser(@RequestParam String token) throws ParseException {
        Boolean isAuthentication = authorizationService.isAuthenticationUser(token);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(isAuthentication);
    }
}
