package com.kamegatze.file.manager.repositories;

import com.kamegatze.file.manager.models.FileSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileSystemRepository extends JpaRepository<FileSystem, UUID> {

    @Query(value = """
        with recursive get_file_system_by_path(id, file, name, user_id, parent_id, is_file, path) as (
            select fs1.id, fs1.file, fs1.name, fs1.user_id, fs1.parent_id, fs1.is_file, fs1.name as path from file_system as fs1
            where name = 'root' and parent_id is null and user_id = (select id from users where login = :login)
            union all
            select fs2.id, fs2.file, fs2.name, fs2.user_id, fs2.parent_id, fs2.is_file, cast(get_file_system_by_path.path || '/' || fs2.name as text) from file_system as fs2
            join get_file_system_by_path on get_file_system_by_path.id = fs2.parent_id
        )
        select *
        from get_file_system_by_path
        where path = :path
    """, nativeQuery = true)
    Optional<FileSystem> getFileSystemByPath(@Param("path") String path, @Param("login") String login);

    @Modifying
    @Query(value = """
    delete from file_system
    where id in (
        with recursive get_children_by_parent_id(id, parent_id) as (
            select fs1.id, fs1.parent_id from file_system as fs1
            where parent_id = 'ac52be4c-f9b5-464a-b299-90a7969d7bfc'
            union all
            select fs2.id, fs2.parent_id from file_system as fs2
            join get_children_by_parent_id on get_children_by_parent_id.id = fs2.parent_id
        )
        select id from get_children_by_parent_id
    )
    """, nativeQuery = true)
    int deleteAllChildrenByParentId(@Param("parentId") String parentId);
}