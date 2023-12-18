package com.kamegatze.authorization.configuration.security.details;

import com.kamegatze.authorization.model.Users;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;


public class UsersDetails implements UserDetails {

    private final Users users;

    private final ModelMapper model = new ModelMapper();

    private final List<? extends GrantedAuthority> authorityList;

    public UsersDetails(Users users) {
        this.users = users;
        this.authorityList = users.getAuthorities()
                .stream().map(item -> new SimpleGrantedAuthority(item.getName().name())).toList();;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityList;
    }

    @Override
    public String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
