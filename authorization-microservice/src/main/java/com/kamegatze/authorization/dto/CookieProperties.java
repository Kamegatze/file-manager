package com.kamegatze.authorization.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("token.cookie.jwt")
public class CookieProperties {
    private CookieInfo accessToken;
    private CookieInfo refreshToken;
}
