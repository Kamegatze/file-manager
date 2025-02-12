package com.kamegatze.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookieInfo {
    private String name;
    private String domain;
    private String path;
    private int maxAge;
    private boolean httpOnly;
    private boolean secure;
    private String sameSite;
}
