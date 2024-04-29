package com.kamegatze.file.manager.dto.filesystem;

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
public class RenameFileSystemDto {
    @NotNull
    @NotEmpty
    @NotBlank
    private String name;
    @NotNull
    private UUID id;
}
