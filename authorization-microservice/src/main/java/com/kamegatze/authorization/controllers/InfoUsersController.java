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
import java.util.UUID;


@Controller
@RequiredArgsConstructor
@Secured(value = {AuthorityConstant.AUTHORITY_READ})
public class InfoUsersController {

    private final UsersRepository usersRepository;

    @QueryMapping
    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    @QueryMapping
    public Users findById(@Argument UUID id) throws UserNotExistException {
        return usersRepository.findById(id)
                .orElseThrow(() -> new UserNotExistException(
                        String.format("User not exist with id: %s", id)
                ));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> handleTest() {
        return ResponseEntity.ok(Map.of("message", "Hello"));
    }
}
