package com.kamegatze.authorization.remote.security.jwt;

import com.kamegatze.authorization.remote.security.filter.model.Authority;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class JwtUtility {

    private JwtUtility() {
    }

    public static List<Authority> getAuthoritiesFromToken(String token) {
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String[] authorityArray = ((String) jwtClaimsSet.getClaim("authority")).split(" ");
        return Arrays.asList(authorityArray).parallelStream()
                .map(item -> new Authority(item.trim())).toList();
    }

    public static String getUsernameFromToken(String token) {
        JWTClaimsSet jwtClaimsSet;
        try {
            jwtClaimsSet = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return jwtClaimsSet.getSubject();
    }

    public static boolean isExpired(String token) {
        try {
            var jwt = JWTParser.parse(token);
            DefaultJWTClaimsVerifier<SecurityContext> claimsVerifier = new DefaultJWTClaimsVerifier<>(
                    jwt.getJWTClaimsSet(), Set.of("iss", "sub", "exp", "iat", "authority")
            );
            claimsVerifier.verify(jwt.getJWTClaimsSet(), new SimpleSecurityContext());
            return false;
        } catch (ParseException | BadJWTException e) {
            return true;
        }
    }
}
