package com.kamegatze.authorization.repoitory;

import com.kamegatze.authorization.model.UsersAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsersAuthorityRepository extends JpaRepository<UsersAuthority, UUID> {
    List<UsersAuthority> findByUsersId(UUID usersId);
}
