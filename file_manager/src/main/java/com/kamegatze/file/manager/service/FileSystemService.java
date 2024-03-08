package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.dto.filesystem.FileDto;
import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.dto.filesystem.FolderDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface FileSystemService {
    FileSystemDto createFolderByFolderParentId(FolderDto fileSystemDto, HttpServletRequest httpServletRequest);
    FileSystemDto createSaveFileByFolderParentId(FileDto fileSystemDto, HttpServletRequest httpServletRequest);
    List<FileSystemDto> getChildrenByParentId(UUID parentId, HttpServletRequest httpServletRequest);
    FileSystemDto getFileSystem(UUID fileSystemId);
    UUID deleteFileSystemById(UUID fileSystemId);
}
