package com.kamegatze.file.manager.configuration.security.expired.token.check;

import com.kamegatze.authorization.remote.security.expired.check.ExpiredCheck;
import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.service.UsersService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ExpiredTokenChecker implements ExpiredCheck {
    private final UsersService usersService;
    @Override
    @Transactional
    public boolean check(String token) {
        String login = getLogin(token);
        Users users = usersService.getUsersByLogin(login);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Instant expiredFromToken = getExpired(token);
        if (Objects.isNull(users.getToken())) {
            users.setToken(passwordEncoder.encode(token));
            users.setExpired(expiredFromToken);
            usersService.updateOrSaveUsers(users);
            return false;
        }
        Instant now = Instant.now();
        if (now.isBefore(users.getExpired())) {
            return true;
        }
        users.setToken(passwordEncoder.encode(token));
        users.setExpired(expiredFromToken);
        usersService.updateOrSaveUsers(users);
        return false;
    }

    private String getLogin(String token) {
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return jwtClaimsSet.getSubject();
    }

    private Instant getExpired(String token) {
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return jwtClaimsSet.getExpirationTime().toInstant();
    }
}
