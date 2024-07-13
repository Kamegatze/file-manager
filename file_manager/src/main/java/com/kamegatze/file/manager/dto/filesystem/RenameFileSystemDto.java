package com.kamegatze.file.manager.dto.filesystem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rename entity")
public class RenameFileSystemDto {
    @NotNull
    @NotEmpty
    @NotBlank
    @Schema(description = "new name for folder or file", example = "title.txt")
    private String name;
    @NotNull
    @Schema(description = "id parent for entity rename name", example = "2e47f203-afe8-4d61-ad52-eac56485c67d")
    private UUID id;
}
