package com.kamegatze.authorization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Login {

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
}
