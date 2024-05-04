package com.kamegatze.file.manager.repositories;

import com.kamegatze.file.manager.models.FileSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileSystemRepository extends JpaRepository<FileSystem, UUID> {
    List<FileSystem> findAllByNameAndUserId(String name, UUID user_id);
}