package com.kamegatze.file.manager.repositories;

import com.kamegatze.file.manager.models.FileSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileSystemRepository extends JpaRepository<FileSystem, UUID> {
}