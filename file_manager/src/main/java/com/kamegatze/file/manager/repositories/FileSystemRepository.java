package com.kamegatze.file.manager.repositories;

import com.kamegatze.file.manager.models.FileSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FileSystemRepository extends JpaRepository<FileSystem, UUID> {

    @Query(value = """
        WITH RECURSIVE get_file_system_by_path(id, file, name, user_id, parent_id, is_file, path) as (
            select fs1.id, fs1.file, fs1.name, fs1.user_id, fs1.parent_id, fs1.is_file, fs1.name as path from file_system as fs1
            where name = 'root' and parent_id is null and user_id = (select id from users where login = :login)
            union all
            select fs2.id, fs2.file, fs2.name, fs2.user_id, fs2.parent_id, fs2.is_file, cast(get_file_system_by_path.path || '/' || fs2.name as text) from file_system as fs2
            join get_file_system_by_path on get_file_system_by_path.id = fs2.parent_id
        )
        SELECT *
        FROM get_file_system_by_path
        where path = :path
    """, nativeQuery = true)
    Optional<FileSystem> getFileSystemByPath(@Param("path") String path, @Param("login") String login);

}