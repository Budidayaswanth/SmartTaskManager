package com.smarttask.smarttask_backend.dto;

import com.smarttask.smarttask_backend.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

/**
 * DTO for creating a new task.
 * Used in TaskController @PostMapping("/api/tasks").
 * @NotBlank ensures title cannot be empty.
 */
public record TaskCreateRequest(
        @NotBlank String title,
        String description,
        OffsetDateTime dueDate,
        TaskStatus status,
        Boolean completed
) {}
