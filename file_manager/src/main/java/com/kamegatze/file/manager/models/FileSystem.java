package com.kamegatze.file.manager.models;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Blob;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileSystem {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private Boolean isFile;

    @Lob
    @Column
    private Blob file;

    @Column
    private String name;

    @Column
    private UUID parentId;

    @Column
    private String path;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
