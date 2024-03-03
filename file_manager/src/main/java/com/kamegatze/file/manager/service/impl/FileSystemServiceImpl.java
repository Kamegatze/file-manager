package com.kamegatze.file.manager.service.impl;

import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.models.Children;
import com.kamegatze.file.manager.models.FileSystem;
import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.repositories.FileSystemRepository;
import com.kamegatze.file.manager.service.FileSystemService;
import com.kamegatze.file.manager.service.JwtService;
import com.kamegatze.file.manager.service.UsersService;
import com.kamegatze.file.manager.utilities.mapper.MapperClazz;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileSystemServiceImpl implements FileSystemService {
    private final FileSystemRepository fileSystemRepository;
    private final UsersService usersService;
    private final JwtService jwtService;
    private final MapperClazz mapperClazz;

    @Override
    public FileSystemDto createFolderByFolderParentId(UUID parentId,
                                                      FileSystemDto fileSystemDto,
                                                      HttpServletRequest request) {

        return null;
    }

    @Override
    public FileSystemDto createSaveFileByFolderParentId(UUID parentId,
                                                        FileSystemDto fileSystemDto,
                                                        HttpServletRequest request) {
        return null;
    }

    @Override
    public List<FileSystemDto> getChildrenByFolderParentId(UUID parentId,
                                                           HttpServletRequest request) {
        Users users = usersService.getUsersByLogin(jwtService.getLogin(request));
        FileSystem fileSystem = users.getFileSystem().stream()
                .filter(fileSystemFilter -> fileSystemFilter.getId().equals(parentId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("FileSystem not found by id: %s", 
                        parentId)
                ));
        List<FileSystem> fileSystems = fileSystem.getChildren().stream().map(Children::getChildren).toList();
        return mapperClazz.mapperToList(fileSystems, FileSystemDto.class);
    }

}
