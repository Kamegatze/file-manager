package com.kamegatze.file.manager.dto.filesystem;

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
    private String name;
    private UUID parentId;
}
