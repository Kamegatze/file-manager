package com.kamegatze.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entity user")
public class UsersDto {
    @Schema(description = "id for user", example = "c6041162-dec9-421d-a5b7-d7443ab99eef",
            accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 5, max = 20, message = "Your login need more 4 and less 20 sign")
    @Schema(description = "unique alias for user", example = "kamegatze")
    private String login;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 8, message = "Your password need more 8 sign")
    @Schema(description = "password user", example = "qwerr1233A____@")
    private String password;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 2, max = 25, message = "Your first name need more 2 and less 25 sign")
    @Schema(description = "first name user", example = "Aleksey")
    private String firstName;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 2, max = 25, message = "Your last name need more 2 and less 25 sign")
    @Schema(description = "last name user", example = "Shirayev")
    private String lastName;

    @NotEmpty
    @NotBlank
    @NotNull
    @Email
    @Schema(description = "email user", example = "kamegatze@kamegatze.com")
    private String email;
}
