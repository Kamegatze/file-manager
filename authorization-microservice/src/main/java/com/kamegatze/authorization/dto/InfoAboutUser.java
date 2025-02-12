package com.kamegatze.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Info type authorization")
public record InfoAboutUser(
        @Schema(description = "login user", example = "kamegatzeTwo") String login,
        @Schema(description = "is use 2fa", example = "false") boolean isEnable2FA
) {
}
