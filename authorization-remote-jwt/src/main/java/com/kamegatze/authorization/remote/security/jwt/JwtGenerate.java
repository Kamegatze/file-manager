package com.kamegatze.authorization.remote.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface JwtGenerate {
    String generateAccess(UserDetails usersDetails);

    String generateAccess(String username, List<GrantedAuthority> grantedAuthorities);

    String generateRefresh(UserDetails usersDetails);

    String generateRefresh(String username, List<GrantedAuthority> grantedAuthorities);
}
