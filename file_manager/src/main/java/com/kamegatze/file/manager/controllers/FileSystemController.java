package com.kamegatze.file.manager.controllers;

import com.kamegatze.file.manager.dto.filesystem.FileSystemDto;
import com.kamegatze.file.manager.service.FileSystemService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file-system")
@RequiredArgsConstructor
public class FileSystemController {
    private final FileSystemService fileSystemService;
    @GetMapping("/children")
    ResponseEntity<List<FileSystemDto>> handleGetChildrenByParentId(@RequestParam UUID parentId,
                                                                    HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fileSystemService.getChildrenByFolderParentId(parentId, request));
    }
}
