package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface FileSystemService {
    FileSystemDto createFolderByFolderParentId(UUID parentId, FileSystemDto fileSystemDto, HttpServletRequest httpServletRequest);
    FileSystemDto createSaveFileByFolderParentId(UUID parentId, FileSystemDto fileSystemDto, HttpServletRequest httpServletRequest);
    List<FileSystemDto> getChildrenByFolderParentId(UUID parentId, HttpServletRequest httpServletRequest);
}
