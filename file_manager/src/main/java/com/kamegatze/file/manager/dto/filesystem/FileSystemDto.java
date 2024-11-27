package com.kamegatze.file.manager.dto.filesystem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entity file system")
public class FileSystemDto {

    @Schema(description = "id file system database", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "answers the question is it a file or a folder", example = "true")
    private Boolean isFile;

    @Schema(description = "Name entity file system", example = "title.txt")
    private String name;

    @Schema(description = "id parent folder", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID parentId;
}
