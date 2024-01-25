package com.kamegatze.authorization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersDto {

    private UUID id;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 5, max = 20, message = "Your login need more 5 and less 20 sign")
    private String login;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 8, message = "Your password need more 8 sign")
    private String password;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 2, max = 25, message = "Your first name need more 2 and less 25 sign")
    private String firstName;

    @NotEmpty
    @NotBlank
    @NotNull
    @Size(min = 2, max = 25, message = "Your last name need more 2 and less 25 sign")
    private String lastName;

    @NotEmpty
    @NotBlank
    @NotNull
    @Email
    private String email;
}
