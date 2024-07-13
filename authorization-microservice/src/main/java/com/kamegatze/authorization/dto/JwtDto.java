package com.kamegatze.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT info for authorization user")
public class JwtDto {
    @Schema(description = "token access",
            example = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRob3JpemF0aW9uLWZpbGUtbWFuYWdlc.......")
    private String tokenAccess;
    @Schema(description = "token for refresh authorization",
            example = "eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJhdXRob......."
    )
    private String refreshToken;
    @Schema(description = "type token",
            example = "Bearer")
    private ETokenType type;
}
