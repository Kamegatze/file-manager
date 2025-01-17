package com.kamegatze.file.manager.controllers;

import com.kamegatze.file.manager.dto.filesystem.*;
import com.kamegatze.file.manager.models.FileSystem;
import com.kamegatze.file.manager.service.FileSystemService;
import com.kamegatze.general.dto.response.ResponseDtoByDelete;
import com.kamegatze.general.dto.template.response.TemplateMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@RequestMapping("/api/v1/file-system")
@Tag(name = "FileSystem", description = "End point for work with file system")
public class FileSystemController {

    private final FileSystemService fileSystemService;


    @GetMapping("/children")
    @Operation(
            summary = "Get children by parent id", description = "Get children by parent id. Get list file system"
    )
    ResponseEntity<List<FileSystemDto>> handleGetChildrenByParentId(
                                                                @RequestParam(name = "parentId")
                                                                @Parameter(description = "id parent folder",
                                                                    name = "parentId",
                                                                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
                                                                    String parentId,
                                                                    HttpServletRequest request) {
        List<FileSystemDto> fileSystems = fileSystemService.getChildrenByParentId(parentId, request);
        return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fileSystems);
    }

    @Operation(
            summary = "Create folder", description = "Create folder via object folder"
    )
    @PostMapping("/create-folder")
    ResponseEntity<FileSystemDto> handleCreateFolder(@RequestBody FolderDto folderDto,
                                                 HttpServletRequest request,
                                                 UriComponentsBuilder builder) {
        FileSystemDto fileSystemDto = fileSystemService.createFolderByFolderParentId(folderDto, request);
        return ResponseEntity.created(
                builder.path("/api/file-system/{fileSystemId}")
                        .build(Map.of("fileSystemId", fileSystemDto.getId()))
        ).contentType(MediaType.APPLICATION_JSON).body(fileSystemDto);

    }

    @Operation(
            summary = "Save file and create entity file in system",
            description = "use post mapping via multipart request use request param"
    )
    @PostMapping(path = "/create-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileSystemDto> handleCreateFile(
            @RequestParam(name = "file") @Parameter(description = "file for save",
                    name = "file", required = true) MultipartFile file,
            @RequestParam(name = "parentId") @Parameter(description = "parent id for save file entity",
                    name = "parentId", example = "empty.txt", required = true) UUID parentId,
            HttpServletRequest request,
            UriComponentsBuilder builder
    ) throws IOException, SQLException {
        FileSystemDto fileSystemDto = fileSystemService.createSaveFileByFolderParentId(
                FileDto.builder()
                        .name(file.getOriginalFilename())
                        .parentId(parentId)
                        .file(new SerialBlob(file.getBytes()))
                        .build(),
                request
        );
        return ResponseEntity.created(
                builder.path("/api/file-system/{fileSystemId}")
                        .build(Map.of("fileSystemId", fileSystemDto.getId()))
        ).contentType(MediaType.APPLICATION_JSON).body(fileSystemDto);
    }

    @Operation(
            summary = "Get folder or file", description = "Get folder or file by id"
    )
    @GetMapping("/{fileSystemId}")
    ResponseEntity<FileSystemDto> handleGetFileSystem(@PathVariable(name = "fileSystemId")
                                                   @Parameter(description = "id folder or file in system",
            name = "fileSystemId", example = "17d278fb-d266-4470-bd8c-9322d9cca93d", required = true) String fileSystemId) {
        FileSystemDto fileSystemDto = fileSystemService.getFileSystem(fileSystemId);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileSystemDto);
    }

    @Operation(
            summary = "Delete folder or file", description = "Delete folder or file by id"
    )
    @DeleteMapping("/{fileSystemId}")
    ResponseEntity<ResponseDtoByDelete> handleDeleteById(@PathVariable(name = "fileSystemId")
                                                         @Parameter(description = "id folder or file",
            name = "fileSystemId", example = "3072d3eb-1dce-4a7c-891e-5a8c3e0f60ea", required = true)
                                                         String fileSystemId) {
        UUID deleteId = fileSystemService.deleteFileSystemById(fileSystemId);
        final ResponseDtoByDelete responseDtoByDelete = ResponseDtoByDelete.builder()
                .deleteId(deleteId)
                .message(String.format(TemplateMessage.messageByDelete, "FileSystem", deleteId))
                .status(HttpStatus.ACCEPTED)
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDtoByDelete);
    }

    @Operation(
            summary = "Download stream file", description = "Download stream file via octet stream"
    )
    @GetMapping("/download/{fileId}")
    ResponseEntity<byte[]> handleDownloadFile(@PathVariable(name = "fileId")
                                              @Parameter(description = "id file",
            name = "fileId", example = "7d6e6971-b55e-417a-87d7-65221651861d", required = true) String fileId) throws SQLException, IOException {
        FileSystem fileSystem = fileSystemService.getFileByFileId(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileSystem.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileSystem.getFile().length())
                .body(fileSystem.getFile().getBinaryStream().readAllBytes());
    }

    @Operation(
            summary = "Get root folder for current user", description = "get root folder via jwt token"
    )
    @GetMapping("/get-root")
    ResponseEntity<FileSystemDto> handleGetRoot(HttpServletRequest request) {
        FileSystemDto fileSystem = fileSystemService.getRoot(request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileSystem);
    }

    @Operation(
            summary = "Get children element by path", description = "return list file system item by path"
    )
    @GetMapping("/children-by-path")
    ResponseEntity<List<FileSystemDto>> handleChildrenByPath(@RequestParam(name = "path")
                                                             @Parameter(description = "path to root folder",
                                                                     name = "path", example = "root/for_example1/for_example2", required = true)
                                                             String path, HttpServletRequest request) {
        List<FileSystemDto> fileSystemDtoList = fileSystemService.getChildrenByPath(path, request);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileSystemDtoList);
    }

    @Operation(
            summary = "Rename folder or file", description = "rename folder or file and all paths children if this folder"
    )
    @PostMapping("/rename-file-system")
    ResponseEntity<FileSystemDto> handleRenameFileSystem(@RequestBody RenameFileSystemDto renameFileSystemDto) {
        FileSystemDto fileSystemDto = fileSystemService.renameFileSystem(renameFileSystemDto);
        return ResponseEntity.ok(fileSystemDto);
    }

    @Operation(
            summary = "Download zip archive with folders and files", description = "Download zip archive with folders and files"
    )
    @GetMapping("/download-all-content-folder/{fileSystemId}")
    ResponseEntity<byte[]> handleDownloadAllContentFolder(
            @PathVariable("fileSystemId") @Parameter(description = "id folder for download",
                    name = "fileSystemId", example = "86621cf3-8f4b-4212-85b4-9520339c3e82", required = true) String fileSystemId) {
        AllContentFolder allContentFolder = fileSystemService.getAllContentFolder(fileSystemId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + allContentFolder.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(allContentFolder.getContent().length)
                .body(allContentFolder.getContent());
    }
}
