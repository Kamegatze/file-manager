package com.kamegatze.authorization.configuration.security.details;

import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.repoitory.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersRepository.findByLogin(username)
                .orElseThrow(() -> new NoSuchElementException(String.format("User not found with login: %s", username)));
        return new UsersDetails(users);
    }
}
