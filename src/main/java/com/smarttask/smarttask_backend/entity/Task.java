package com.smarttask.smarttask_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/** WHY: User-owned tasks with due date and flags. */
@Entity @Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Deprecated
    @Column(nullable = false)
    private boolean completed;

    private OffsetDateTime dueDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        syncStatusAndCompleted();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        syncStatusAndCompleted();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.completed = status == TaskStatus.DONE;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.status = TaskStatus.DONE;
        } else if (this.status == null || this.status == TaskStatus.DONE) {
            this.status = TaskStatus.TODO;
        }
    }

    private void syncStatusAndCompleted() {
        if (status == null) {
            status = completed ? TaskStatus.DONE : TaskStatus.TODO;
        }
        completed = status == TaskStatus.DONE;
    }
}
