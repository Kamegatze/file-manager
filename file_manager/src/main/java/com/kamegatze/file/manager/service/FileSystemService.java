package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.dto.filesystem.FileDto;
import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.dto.filesystem.FolderDto;
import com.kamegatze.file.manager.models.FileSystem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;


import java.util.List;
import java.util.UUID;

@Validated
public interface FileSystemService {
    FileSystemDto createFolderByFolderParentId(@Valid FolderDto fileSystemDto,
                                               HttpServletRequest httpServletRequest);
    FileSystemDto createSaveFileByFolderParentId(@Valid FileDto fileSystemDto,
                                                 HttpServletRequest httpServletRequest);
    List<FileSystemDto> getChildrenByParentId(@NotNull UUID parentId,
                                              HttpServletRequest httpServletRequest);
    FileSystemDto getFileSystem(@NotNull UUID fileSystemId);
    FileSystem getFileByFileId(@NotNull UUID fileId);
    UUID deleteFileSystemById(@NotNull UUID fileSystemId);
}
