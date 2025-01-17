package com.kamegatze.file.manager.configuration.security.details;

import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.service.UsersService;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
