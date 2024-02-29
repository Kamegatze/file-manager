package com.kamegatze.authorization.remote.security.authentication.token;

import com.nimbusds.jwt.JWT;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public class JwtRemoteAuthenticationToken<T extends JWT> extends AbstractAuthenticationToken {
    private T token;
    private Object principal;
    private Object credentials;

    public JwtRemoteAuthenticationToken(T token) {
        this(token, null);
    }

    public JwtRemoteAuthenticationToken(T token, Collection<? extends GrantedAuthority> authorities) {
        this(token, token, token, authorities);
    }

    private JwtRemoteAuthenticationToken(T token, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(token, "token cannot be null");
        Assert.notNull(principal, "principal cannot be null");
        this.principal = principal;
        this.credentials = credentials;
        this.token = token;
    }

    public JwtRemoteAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public T getToken() {
        return token;
    }
    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
