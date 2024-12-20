package com.kamegatze.file.manager.dto.filesystem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    @NotNull
    @NotEmpty
    @NotBlank
    private String name;
    @NotNull
    private UUID parentId;
}
