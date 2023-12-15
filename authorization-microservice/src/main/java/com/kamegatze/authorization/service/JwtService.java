package com.kamegatze.authorization.service;

import org.springframework.security.core.Authentication;

public interface JwtService {
    String generate(Authentication authentication);
}
