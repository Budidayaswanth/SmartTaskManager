package com.smarttask.smarttask_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * WHY:
 * - @Entity maps this class to the "users" table (created by Flyway).
 * - UUID id via @UuidGenerator (modern Hibernate 6+, no deprecated GenericGenerator).
 * - Simple audit with createdAt/updatedAt.
 */
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (role == null) role = "USER";
        enabled = true;
        deleted = false;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}