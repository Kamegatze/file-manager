package com.kamegatze.file.manager.repositories;

import com.kamegatze.file.manager.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {
}