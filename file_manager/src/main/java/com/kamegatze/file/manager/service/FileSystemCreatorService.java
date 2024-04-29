package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.models.FileSystem;

public interface FileSystemCreatorService {
    byte[] creatZipArchiveFromFolderInDatabase(FileSystem fileSystem);
}
