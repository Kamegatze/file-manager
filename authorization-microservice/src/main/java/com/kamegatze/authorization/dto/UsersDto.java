package com.kamegatze.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersDto {

    private UUID id;

    private String login;

    private String password;

    private String firstName;

    private String lastName;

    private String email;
}
