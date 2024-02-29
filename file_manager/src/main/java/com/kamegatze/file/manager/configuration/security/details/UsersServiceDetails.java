package com.kamegatze.file.manager.configuration.security.details;

import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.repositories.UsersRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.NoSuchElementException;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UsersServiceDetails implements UserDetailsService {
    private String token;
    private UsersRepository usersRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Example<Users> example = Example.of(Users.builder()
            .login(username)
            .build()
        );
        Users users = usersRepository.findOne(example)
                .orElseThrow(
                        () -> new NoSuchElementException(String.format("Users not found by login: [%s]", username))
                );
        return UsersDetails.builder()
                .token(token)
                .users(users)
                .build();
    }
}
