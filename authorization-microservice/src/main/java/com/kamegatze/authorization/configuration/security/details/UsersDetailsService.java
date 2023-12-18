package com.kamegatze.authorization.configuration.security.details;

import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.model.UsersAuthority;
import com.kamegatze.authorization.repoitory.AuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersAuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final UsersAuthorityRepository usersAuthorityRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersRepository.findByLogin(username)
                .orElseThrow(() -> new NoSuchElementException(String.format("User not found with login: %s", username)));
        List<UUID> usersAuthorities = usersAuthorityRepository.findByUsersId(users.getId())
                .stream().map(UsersAuthority::getAuthorityId).toList();
        List<? extends GrantedAuthority> authorities = authorityRepository.findAllByIdIn(usersAuthorities)
                .stream().map(item -> new SimpleGrantedAuthority(item.getName().name())).toList();
        return new UsersDetails(users, authorities);
    }
}
