package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.dto.filesystem.FileDto;
import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.dto.filesystem.FolderDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface FileSystemService {
    FolderDto createFolderByFolderParentId(FolderDto fileSystemDto, HttpServletRequest httpServletRequest);
    FileDto createSaveFileByFolderParentId(FileDto fileSystemDto, HttpServletRequest httpServletRequest);
    List<FileSystemDto> getChildrenByFolderParentId(UUID parentId, HttpServletRequest httpServletRequest);
    FileSystemDto getFileSystem(UUID fileSystemId);
}
