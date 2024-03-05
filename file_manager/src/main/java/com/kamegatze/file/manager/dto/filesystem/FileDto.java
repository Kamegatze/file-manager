package com.kamegatze.file.manager.dto.filesystem;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Blob;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private UUID id;
    private Blob file;
    private String name;
    private UUID parentId;
}
