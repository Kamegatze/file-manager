package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.AuthorityDto;
import com.kamegatze.authorization.services.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/authentication/micro-service")
@RequiredArgsConstructor
@Tag(name = "AuthenticationMicroService", description = "Work with authentication for microservice")
public class AuthenticationMicroServiceController {

    private final AuthorizationService authorizationService;

    @Operation(
        summary = "Get authority via token access",
        description = "Get authority via token access in headers request"
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/is-authentication")
    public ResponseEntity<List<AuthorityDto>> handleIsAuthentication(HttpServletRequest request) throws ParseException {
        List<AuthorityDto> authorityDtos = authorizationService.getAuthorityByRequest(request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(authorityDtos);
    }

}
