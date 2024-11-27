package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.dto.filesystem.*;
import com.kamegatze.file.manager.models.FileSystem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface FileSystemService {
    FileSystemDto createFolderByFolderParentId(@Valid FolderDto fileSystemDto,
                                               HttpServletRequest httpServletRequest);
    FileSystemDto createSaveFileByFolderParentId(@Valid FileDto fileSystemDto,
                                                 HttpServletRequest httpServletRequest);
    List<FileSystemDto> getChildrenByParentId(@NotEmpty @NotBlank @NotNull
                                              @Size(min = 36, max = 36, message = "The uuid need consist from 36 sign")
                                              @Pattern(
                                                      regexp = "^[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$",
                                                      message = "incorrect uuid"
                                              )
                                              String parentId,
                                              HttpServletRequest httpServletRequest);
    FileSystemDto getFileSystem(@NotEmpty @NotBlank @NotNull
                                @Size(min = 36, max = 36, message = "The uuid need consist from 36 sign")
                                @Pattern(
                                        regexp = "^[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$",
                                        message = "incorrect uuid"
                                )
                                String fileSystemId);
    FileSystem getFileByFileId(@NotEmpty @NotBlank @NotNull
                               @Size(min = 36, max = 36, message = "The uuid need consist from 36 sign")
                               @Pattern(
                                       regexp = "^[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$",
                                       message = "incorrect uuid"
                               )
                               String fileId);
    UUID deleteFileSystemById(@NotEmpty @NotBlank @NotNull
                              @Size(min = 36, max = 36, message = "The uuid need consist from 36 sign")
                              @Pattern(
                                      regexp = "^[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$",
                                      message = "incorrect uuid"
                              )
                              String fileSystemId);

    FileSystemDto getRoot(HttpServletRequest request);

    List<FileSystemDto> getChildrenByPath(@NotEmpty @NotBlank @NotNull String path, HttpServletRequest request);

    FileSystemDto renameFileSystem(@Valid RenameFileSystemDto renameFileSystemDto);

    AllContentFolder getAllContentFolder(@NotEmpty @NotBlank @NotNull
                                         @Size(min = 36, max = 36, message = "The uuid need consist from 36 sign")
                                         @Pattern(
                                                 regexp = "^[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}$",
                                                 message = "incorrect uuid"
                                         ) String fileSystemId);
}
