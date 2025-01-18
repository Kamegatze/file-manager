package com.kamegatze.authorization.services.imp;

import com.kamegatze.authorization.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
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
    public String generateAccess(UserDetails usersDetails) {
        return generateToken(usersDetails.getUsername(), usersDetails.getAuthorities(), timeAccess);
    }

    @Override
    public String generateAccess(String username, List<GrantedAuthority> grantedAuthorities) {
        return generateToken(username, grantedAuthorities, timeAccess);
    }

    @Override
    public String generateRefresh(UserDetails usersDetails) {
        return generateToken(usersDetails.getUsername(), usersDetails.getAuthorities(), timeRefresh);
    }

    @Override
    public String generateRefresh(String username, List<GrantedAuthority> grantedAuthorities) {
        return generateToken(username, grantedAuthorities, timeRefresh);
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


    private String generateToken(String username, Collection<? extends GrantedAuthority> grantedAuthorities, Integer time) {
        Instant now = Instant.now();
        StringBuilder stringBuilder = new StringBuilder();
        for (GrantedAuthority authority : grantedAuthorities) {
            stringBuilder.append(authority.getAuthority()).append(" ");
        }
        String authorities = stringBuilder.toString();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(time, ChronoUnit.MINUTES))
                .subject(username)
                .claim("authority", authorities)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
