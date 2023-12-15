package com.kamegatze.authorization.service.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtServiceImp implements JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.name-application}")
    private String issuer;

    @Value("${app.access-token}")
    private Integer timeAccess;

    @Value("${app.refresh-token}")
    private Integer timeRefresh;

    @Override
    public String generateAccess(Authentication authentication) {
        return generateToken(authentication, timeAccess);
    }

    @Override
    public String generateRefresh(Authentication authentication) {
        return generateToken(authentication, timeRefresh);
    }

    private String generateToken(Authentication authentication, Integer time) {
        UsersDetails usersDetails = (UsersDetails) authentication.getPrincipal();
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(time, ChronoUnit.MINUTES))
                .subject(usersDetails.getUsername())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
