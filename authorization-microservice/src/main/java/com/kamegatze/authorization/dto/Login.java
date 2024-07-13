package com.kamegatze.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "entity for login user")
public class Login {

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 5, max = 20, message = "Your login need more 5 and less 20 sign")
    @Schema(description = "login user", example = "kamegatzeTwo")
    private String login;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 6, message = "Your password need more 6 sign")
    @Schema(description = "password or code via 2fa", example = "qwerr1233A____@")
    private String credentials;
}
