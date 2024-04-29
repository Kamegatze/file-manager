package com.kamegatze.file.manager.dto.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllContentFolder {
    private String name;
    private byte[] content;
}
