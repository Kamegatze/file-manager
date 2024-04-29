package com.kamegatze.file.manager.service.impl;

import com.kamegatze.file.manager.dto.filesystem.AllContentFolder;
import com.kamegatze.file.manager.dto.filesystem.FileDto;
import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.dto.filesystem.FolderDto;
import com.kamegatze.file.manager.dto.filesystem.RenameFileSystemDto;
import com.kamegatze.file.manager.exception.FileSystemExistByNameAndUserException;
import com.kamegatze.file.manager.models.FileSystem;
import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.repositories.FileSystemRepository;
import com.kamegatze.file.manager.service.FileSystemCreatorService;
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
    private final FileSystemCreatorService fileSystemCreatorService;

    private final String root = "root";
    @Override
    public FileSystemDto createFolderByFolderParentId(FolderDto fileSystemDto,
                                                  HttpServletRequest request) {
        log.info("Start operation save folder: {}", fileSystemDto);
        FileSystem fileSystem = mapperClazz.mapperToClazz(fileSystemDto, FileSystem.class);
        Users users = usersService.getUsersByLogin(jwtService.getLogin(request));

        FileSystem fileSystemCheck = FileSystem.builder()
                .parentId(fileSystem.getParentId())
                .name(fileSystem.getName())
                .user(users)
                .build();
        checkExistFileSystem(fileSystemCheck, "Folder");

        fileSystem.setUser(users);
        fileSystem.setIsFile(Boolean.FALSE);
        fileSystem.setPath(getPath(getFileSystemById(fileSystemDto.getParentId()), fileSystemDto.getName()));
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

        FileSystem fileSystemCheck = FileSystem.builder()
                .parentId(fileSystem.getParentId())
                .name(fileSystem.getName())
                .user(users)
                .build();
        checkExistFileSystem(fileSystemCheck, "File");

        fileSystem.setUser(users);
        fileSystem.setIsFile(Boolean.TRUE);
        fileSystem.setPath(getPath(getFileSystemById(fileSystemDto.getParentId()), fileSystemDto.getName()));
        fileSystem = fileSystemRepository.save(fileSystem);
        log.info("End operation save file: {}", fileSystemDto);
        return mapperClazz.mapperToClazz(fileSystem, FileSystemDto.class);
    }

    @Override
    public List<FileSystemDto> getChildrenByParentId(String parentId,
                                                           HttpServletRequest request) {
        log.info("Start operation extracts children by parentId: {}", parentId);
        Users users = usersService.getUsersByLogin(jwtService.getLogin(request));
        Example<FileSystem> requestFileSystem = Example.of(
                FileSystem.builder()
                        .parentId(UUID.fromString(parentId))
                        .user(users)
                        .build()
        );
        log.info("End operation extracts children by parentId: {}", parentId);
        return mapperClazz.mapperToList(fileSystemRepository.findAll(requestFileSystem), FileSystemDto.class);
    }

    @Override
    public FileSystemDto getFileSystem(String fileSystemId) {
        log.info("Start operation extracts fileSystem by fileSystemId: {}", fileSystemId);
        FileSystem fileSystem = getFileSystemById(UUID.fromString(fileSystemId));
        log.info("End operation extracts fileSystem by fileSystemId: {}", fileSystemId);
        return mapperClazz.mapperToClazz(fileSystem, FileSystemDto.class);
    }

    @Override
    public UUID deleteFileSystemById(String fileSystemId) {
        final UUID fileSystemUUID = UUID.fromString(fileSystemId);
        fileSystemRepository.deleteAllChildrenByParentId(fileSystemUUID);
        fileSystemRepository.deleteById(fileSystemUUID);
        return fileSystemUUID;
    }

    @Override
    public FileSystem getFileByFileId(String fileId) {
        return getFileSystemById(UUID.fromString(fileId));
    }

    @Override
    public FileSystemDto getRoot(HttpServletRequest request) {
        String login = jwtService.getLogin(request);
        Example<FileSystem> requestRoot = Example.of(
                FileSystem.builder()
                        .user(usersService.getUsersByLogin(login))
                        .name(root)
                        .isFile(Boolean.FALSE)
                        .build()
        );
        FileSystem fileSystem = fileSystemRepository.findOne(requestRoot)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(
                                "FileSystem not found by {login: %s}, by {name: %s} and by {isFile: %s}",
                        login, root, Boolean.FALSE)
                ));
        return mapperClazz.mapperToClazz(fileSystem, FileSystemDto.class);
    }

    @Override
    public List<FileSystemDto> getChildrenByPath(String path, HttpServletRequest request) {
        String login = jwtService.getLogin(request);
        FileSystem fileSystem = fileSystemRepository.getFileSystemByPath(path, login)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(
                                "FileSystem not found by {login: %s}, by {path: %s} and by {isFile: %s}",
                                login, path, Boolean.FALSE)
                ));
        return getChildrenByParentId(fileSystem.getId().toString(), request);
    }

    @Override
    public FileSystemDto renameFileSystem(RenameFileSystemDto renameFileSystemDto) {
        FileSystem fileSystem = getFileSystemById(renameFileSystemDto.getId());
        fileSystem.setName(renameFileSystemDto.getName());
        return mapperClazz.mapperToClazz(fileSystemRepository.save(fileSystem), FileSystemDto.class);
    }

    @Override
    public AllContentFolder getAllContentFolder(String fileSystemId) {
        byte[] content = fileSystemCreatorService.creatZipArchiveFromFolderInDatabase(getFileSystemById(UUID.fromString(fileSystemId)));
        String name = getFileSystemById(UUID.fromString(fileSystemId)).getName() + ".zip";
        return AllContentFolder.builder()
                .content(content)
                .name(name)
                .build();
    }

    private String getPath(FileSystem parent, String nameChildren) {
        if (parent.getName().equals("root")) {
            return String.format("%s%s", parent.getPath(), nameChildren);
        }
        return String.format("%s/%s", parent.getPath(), nameChildren);
    }

    private FileSystem getFileSystemById(UUID id) {
        return fileSystemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("FileSystem not found by id: %s", id)
                ));
    }

    private void checkExistFileSystem(FileSystem fileSystem, String nameEntity) {
        Example<FileSystem> searchByUserAndFolderName = Example.of(fileSystem);
        fileSystemRepository.findOne(searchByUserAndFolderName).ifPresent((fileSystemFind) -> {
            throw new FileSystemExistByNameAndUserException(
                    String.format("%s exist by {userId: %s}, by {parentId: %s} and by {file_name: %s}",
                            nameEntity,
                            fileSystem.getUser().getId(),
                            fileSystem.getParentId(),
                            fileSystem.getName()
                    )
            );
        });
    }
}
