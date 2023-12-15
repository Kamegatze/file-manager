package com.kamegatze.authorization.service.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtServiceImp implements JwtService {

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    @Value("${app.name-application}")
    private String issuer;

    @Value("${app.access-token}")
    private Integer timeAccess;

    @Value("${app.refresh-token}")
    private Integer timeRefresh;

    @Override
    public String generateAccess(UsersDetails usersDetails) {
        return generateToken(usersDetails, timeAccess);
    }

    @Override
    public String generateRefresh(UsersDetails usersDetails) {
        return generateToken(usersDetails, timeRefresh);
    }

    @Override
    public String getLogin(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    @Override
    public Instant getIssuedAt(String token) {
        return jwtDecoder.decode(token).getIssuedAt();
    }

    @Override
    public Instant getExpiresAt(String token) {
        return jwtDecoder.decode(token).getExpiresAt();
    }

    @Override
    public Map<String, Object> getClaims(String token) {
        return jwtDecoder.decode(token).getClaims();
    }

    @Override
    public Map<String, Object> getHeaders(String token) {
        return jwtDecoder.decode(token).getHeaders();
    }


    private String generateToken(UsersDetails usersDetails, Integer time) {
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
