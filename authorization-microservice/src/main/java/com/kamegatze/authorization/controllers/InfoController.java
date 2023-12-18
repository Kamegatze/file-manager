package com.kamegatze.authorization.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/info")
public class InfoController {

    @GetMapping("/id")
    public String test() {
        return "Hello";
    }

}
