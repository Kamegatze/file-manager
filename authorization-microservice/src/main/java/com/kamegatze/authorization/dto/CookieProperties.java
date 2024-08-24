package com.kamegatze.authorization.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("token.cookie")
@Data
public class CookieProperties {
    private String name;
    private String domain;
    private String path;
    private int maxAge;
    private boolean httpOnly;
    private boolean secure;
}
