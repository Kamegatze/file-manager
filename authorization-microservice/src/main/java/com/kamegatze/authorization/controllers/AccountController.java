package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.mfa.MFADto;
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.general.dto.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Work with account user")
public class AccountController {

    private final AuthorizationService authorizationService;

    @Operation(
            summary = "Generate secret key for 2fa",
            description = "Generate secret key for 2fa and save secret key in database"
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/set-2fa-authentication")
    public ResponseEntity<MFADto> handleSet2FAAuthentication(HttpServletRequest request) {
        return ResponseEntity.ok(authorizationService.set2FAAuthentication(request));
    }

    @Operation(
            summary = "Check code generate via secret key and turn on 2fa",
            description = "Check code generate via secret key and turn on 2fa"
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/save-2fa-authentication")
    public ResponseEntity<ResponseDto> handleSave2FAAuthentication(@RequestParam(name = "code", required = true)
                                                                   @Parameter(description = "code generation via authenticator",
                                                                           name = "code",
                                                                           example = "564236", required = true)
                                                                   String code,
                                                                   HttpServletRequest request) {
        authorizationService.checkMFAValidateCodeAndEnableAuthorizationViaMFA(code, request);
        return ResponseEntity.ok(null);
    }
}
