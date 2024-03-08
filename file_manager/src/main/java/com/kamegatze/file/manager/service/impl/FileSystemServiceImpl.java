package com.kamegatze.file.manager.service.impl;

import com.kamegatze.file.manager.dto.filesystem.FileDto;
import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.dto.filesystem.FolderDto;
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
import org.springframework.data.domain.Example;
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
    public FileSystemDto createFolderByFolderParentId(FolderDto fileSystemDto,
                                                  HttpServletRequest request) {
        log.info("Start operation save folder: {}", fileSystemDto);
        FileSystem fileSystem = mapperClazz.mapperToClazz(fileSystemDto, FileSystem.class);
        Users users = usersService.getUsersByLogin(jwtService.getLogin(request));
        fileSystem.setUser(users);
        fileSystem.setIsFile(Boolean.FALSE);
        fileSystem = fileSystemRepository.save(fileSystem);
        log.info("End operation save folder: {}", fileSystemDto);
        return mapperClazz.mapperToClazz(fileSystem, FileSystemDto.class);
    }

    @Override
    public FileSystemDto createSaveFileByFolderParentId(FileDto fileSystemDto,
                                                  HttpServletRequest request) {
        log.info("Start operation save file: {}", fileSystemDto);
        Users users = usersService.getUsersByLogin(jwtService.getLogin(request));
        FileSystem fileSystem = mapperClazz.mapperToClazz(fileSystemDto, FileSystem.class);
        fileSystem.setUser(users);
        fileSystem.setIsFile(Boolean.TRUE);
        fileSystem = fileSystemRepository.save(fileSystem);
        log.info("End operation save file: {}", fileSystemDto);
        return mapperClazz.mapperToClazz(fileSystem, FileSystemDto.class);
    }

    @Override
    public List<FileSystemDto> getChildrenByParentId(UUID parentId,
                                                           HttpServletRequest request) {
        log.info("Start operation extracts children by parentId: {}", parentId);
        Users users = usersService.getUsersByLogin(jwtService.getLogin(request));
        Example<FileSystem> requestFileSystem = Example.of(
                FileSystem.builder()
                        .parentId(parentId)
                        .user(users)
                        .build()
        );
        log.info("End operation extracts children by parentId: {}", parentId);
        return mapperClazz.mapperToList(fileSystemRepository.findAll(requestFileSystem), FileSystemDto.class);
    }

    @Override
    public FileSystemDto getFileSystem(UUID fileSystemId) {
        log.info("Start operation extracts fileSystem by fileSystemId: {}", fileSystemId);
        FileSystem fileSystem = fileSystemRepository.findById(fileSystemId)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("FileSystem not found by id: %s", fileSystemId)
                ));
        log.info("End operation extracts fileSystem by fileSystemId: {}", fileSystemId);
        return mapperClazz.mapperToClazz(fileSystem, FileSystemDto.class);
    }

    @Override
    public UUID deleteFileSystemById(UUID fileSystemId) {
        fileSystemRepository.deleteById(fileSystemId);
        return fileSystemId;
    }
}
