package com.kamegatze.file.manager.dto.filesystem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Folder entity")
public class FolderDto {
    @Schema(description = "id folder in system. In request on create folder not use",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            accessMode = Schema.AccessMode.READ_ONLY)

    private UUID id;
    @NotNull
    @NotEmpty
    @NotBlank
    @Schema(description = "name folder", example = "my document")
    private String name;
    @NotNull
    @Schema(description = "parent id folder", example = "dbb07abd-67a7-4983-858c-619b64eef683")
    private UUID parentId;
}
