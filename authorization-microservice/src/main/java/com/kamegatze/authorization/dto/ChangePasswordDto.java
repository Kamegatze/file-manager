package com.kamegatze.authorization.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ChangePasswordDto {
    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 8, message = "Your password need more 8 sign")
    private String password;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 8, message = "Your password retry need more 8 sign")
    private String passwordRetry;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 36, max = 36, message = "The recovery code need consist from 36 sign")
    @Pattern(
            regexp = "^[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$",
            message = "Your recovery code need is uuid"
    )
    private String recoveryCode;
}
