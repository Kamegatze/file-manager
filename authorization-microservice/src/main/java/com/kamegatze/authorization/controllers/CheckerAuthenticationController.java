package com.kamegatze.authorization.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Checker Authentication", description = "Controller for check authenticate user in system")
public class CheckerAuthenticationController {

    @Operation(
            summary = "Check user is authentication via filters authorization spring security"
    )
    @GetMapping("/is-authentication")
    public ResponseEntity<Void> handleIsAuthenticationUser() {
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

}
