package com.kamegatze.file.manager.controllers;

import com.kamegatze.file.manager.dto.filesystem.FileDto;
import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.dto.filesystem.FolderDto;
import com.kamegatze.file.manager.service.FileSystemService;
import com.kamegatze.file.manager.utilities.mapper.MapperClazz;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file-system")
@RequiredArgsConstructor
public class FileSystemController {
    private final FileSystemService fileSystemService;
    private final MapperClazz mapperClazz;
    @GetMapping(value = "/children")
    ResponseEntity<List<FileSystemDto>> handleGetChildrenByParentId(@RequestParam UUID parentId,
                                                                    HttpServletRequest request) {
        List<FileSystemDto> fileSystems = fileSystemService.getChildrenByFolderParentId(parentId, request);
        return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fileSystems);
    }

    @PostMapping("/create-folder")
    ResponseEntity<FolderDto> handleCreateFolder(@RequestBody FolderDto folderDto,
                                                 HttpServletRequest request) {
        return ResponseEntity.ok(fileSystemService.createFolderByFolderParentId(
                folderDto, request
        ));
    }

    @PostMapping("/create-file")
    ResponseEntity<FileSystemDto> handleCreateFile(
            @RequestParam MultipartFile file,
            @RequestParam UUID parentId,
            HttpServletRequest request
    ) throws IOException, SQLException {
        FileDto fileDto = fileSystemService.createSaveFileByFolderParentId(
                FileDto.builder()
                        .name(file.getOriginalFilename())
                        .parentId(parentId)
                        .file(new SerialBlob(file.getBytes()))
                        .build(),
                request
        );
        return ResponseEntity.ok(mapperClazz.mapperToClazz(fileDto, FileSystemDto.class));
    }

    @GetMapping("/{fileSystemId}")
    ResponseEntity<FileSystemDto> handleFolderById(@PathVariable UUID fileSystemId) {
        FileSystemDto fileSystem = fileSystemService.getFileSystem(fileSystemId);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileSystem);
    }
}
