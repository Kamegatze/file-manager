package com.kamegatze.file.manager.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
