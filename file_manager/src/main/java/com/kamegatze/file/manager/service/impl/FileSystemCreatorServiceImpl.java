package com.kamegatze.file.manager.service.impl;

import com.kamegatze.file.manager.models.FileSystem;
import com.kamegatze.file.manager.repositories.FileSystemRepository;
import com.kamegatze.file.manager.service.FileSystemCreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
@RequiredArgsConstructor
public class FileSystemCreatorServiceImpl implements FileSystemCreatorService {

    private final FileSystemRepository fileSystemRepository;
    private static final String PATH_DEFAULT = "./file_manager/src/main/resources/temp-files-for-download";

    @Override
    public byte[] creatZipArchiveFromFolderInDatabase(FileSystem fileSystem) {
        try {
            String path = String.format("%s/%s/%s", PATH_DEFAULT, fileSystem.getUser().getId().toString(), fileSystem.getId());
            createFolder(path);
            createZipArchive(Paths.get(String.format("%s%s", path, fileSystem.getPath())), fileSystem);
            String pathZip = String.format("%s%s.zip", path, fileSystem.getPath());
            try(FileInputStream fileInputStream = new FileInputStream(pathZip)) {
                return fileInputStream.readAllBytes();
            }
        } catch (IOException | SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void createZipArchive(Path path, FileSystem fileSystem) throws IOException, SQLException {
        FileOutputStream outputStream = new FileOutputStream(String.format("%s.zip", path));
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        zipOutputStream.putNextEntry(new ZipEntry(fileSystem.getPath() + "/"));
        zipOutputStream.closeEntry();

        List<FileSystem> allChildren = fileSystemRepository.getAllChildrenByParentId(fileSystem.getId());
        for (FileSystem item : allChildren) {
            if (item.getIsFile()) {
                zipOutputStream.putNextEntry(new ZipEntry(item.getPath()));
                zipOutputStream.write(item.getFile().getBinaryStream().readAllBytes());
                zipOutputStream.closeEntry();
            } else {
                zipOutputStream.putNextEntry(new ZipEntry(item.getPath() + "/"));
                zipOutputStream.closeEntry();
            }
        }

        zipOutputStream.close();
        outputStream.close();
    }

    private void createFolder(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }
}
