package com.kamegatze.authorization.repoitory;

import com.kamegatze.authorization.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByLogin(String login);
    Boolean existsByLogin(String login);
    Optional<Users> findByEmail(String email);
    Boolean existsByEmail(String email);
}
