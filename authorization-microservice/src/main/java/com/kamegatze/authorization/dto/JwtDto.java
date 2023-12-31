package com.kamegatze.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtDto {
    private String tokenAccess;
    private String refreshToken;
    private ETokenType type;
}
