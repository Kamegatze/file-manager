package com.kamegatze.authorization.controllers;

import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.model.AuthorityConstant;
import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.repoitory.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Secured(value = {AuthorityConstant.AUTHORITY_READ})
public class InfoUsersController {

    private final UsersRepository usersRepository;

    @QueryMapping
    @Secured({AuthorityConstant.AUTHORITY_WRITE})
    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    @QueryMapping
    public Users findByLogin(@Argument String login) throws UserNotExistException {
        return usersRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotExistException(
                        String.format("User not exist with login: %s", login)
                ));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> handleTest() {
        return ResponseEntity.ok(Map.of("message", "Hello"));
    }
}
