package com.kamegatze.file.manager.dto.filesystem;

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
public class FolderDto {
    private UUID id;
    @NotNull
    @NotEmpty
    @NotBlank
    private String name;
    @NotNull
    private UUID parentId;
}
