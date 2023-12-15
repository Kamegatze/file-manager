package com.kamegatze.authorization.repoitory;

import com.kamegatze.authorization.model.Authority;
import com.kamegatze.authorization.model.EAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
    Optional<Authority> findByName(EAuthority authority);
}
