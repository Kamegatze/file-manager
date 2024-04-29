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

    /**
     * Method for get FileSystem {@link FileSystem} item by login and path
     * @param login login user for search file-system
     * @param path path file-system
     * @return {@link FileSystem} find
     * */
    Optional<FileSystem> findByUser_LoginAndPath(String login, String path);


    /**
     * Method for get all children by parentId
     * @param parentId - id parent FileSystem
     * @return {@link FileSystem} list file system
     * */
    @Query(value = """
    select * from file_system
    where id in (
        with recursive get_children_by_parent_id(id, parent_id) as (
            select fs1.id, fs1.parent_id from file_system as fs1
            where parent_id = :parentId
            union all
            select fs2.id, fs2.parent_id from file_system as fs2
            join get_children_by_parent_id on get_children_by_parent_id.id = fs2.parent_id
        )
        select id from get_children_by_parent_id
    )
    """, nativeQuery = true)
    List<FileSystem> getAllChildrenByParentId(@Param("parentId") UUID parentId);

    /**
     * Method for delete all children item in file system
     * @param parentId id parent FileSystem {@link FileSystem}
     * */
    @Modifying
    @Query(value = """
    delete from file_system
    where id in (
        with recursive get_children_by_parent_id(id, parent_id) as (
            select fs1.id, fs1.parent_id from file_system as fs1
            where parent_id = :parentId
            union all
            select fs2.id, fs2.parent_id from file_system as fs2
            join get_children_by_parent_id on get_children_by_parent_id.id = fs2.parent_id
        )
        select id from get_children_by_parent_id
    )
    """, nativeQuery = true)
    void deleteAllChildrenByParentId(@Param("parentId") UUID parentId);
}