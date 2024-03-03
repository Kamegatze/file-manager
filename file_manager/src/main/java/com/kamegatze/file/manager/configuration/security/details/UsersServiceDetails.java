package com.kamegatze.file.manager.configuration.security.details;

import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.repositories.UsersRepository;
import com.kamegatze.file.manager.service.UsersService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.NoSuchElementException;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UsersServiceDetails implements UserDetailsService {
    private String token;
    private UsersService usersService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersService.getUsersByLogin(username);
        return UsersDetails.builder()
                .token(token)
                .users(users)
                .build();
    }
}
