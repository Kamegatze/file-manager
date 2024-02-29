package com.kamegatze.file.manager.repositories;

import com.kamegatze.file.manager.models.Children;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChildrenRepository extends JpaRepository<Children, UUID> {
}