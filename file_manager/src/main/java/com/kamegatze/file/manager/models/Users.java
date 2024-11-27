package com.kamegatze.file.manager.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Builder
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @Column
    private UUID id;

    @Column
    private String login;

    @Column
    private String email;

    @Column
    private String token;

    @Column
    private Instant expired;

    @OneToMany(mappedBy = "user")
    private List<FileSystem> fileSystem;
}
