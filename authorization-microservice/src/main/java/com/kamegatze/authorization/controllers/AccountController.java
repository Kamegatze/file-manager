package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.dto.mfa.MFADto;
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.general.dto.response.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AuthorizationService authorizationService;

    @GetMapping("/set-2fa-authentication")
    public ResponseEntity<MFADto> handleSet2FAAuthentication(HttpServletRequest request) {
        return ResponseEntity.ok(authorizationService.set2FAAuthentication(request));
    }

    @GetMapping("/save-2fa-authentication")
    public ResponseEntity<ResponseDto> handleSave2FAAuthentication(@RequestParam String code,
                                                                   HttpServletRequest request) {
        authorizationService.checkMFAValidateCodeAndEnableAuthorizationViaMFA(code, request);
        return ResponseEntity.ok(null);
    }
}
