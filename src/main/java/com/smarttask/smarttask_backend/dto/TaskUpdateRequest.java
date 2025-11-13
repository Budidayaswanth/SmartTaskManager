package com.smarttask.smarttask_backend.dto;

import com.smarttask.smarttask_backend.entity.TaskStatus;

import java.time.OffsetDateTime;

/**
 * DTO for updating an existing task.
 * All fields optional — only non-null fields will be updated.
 */
public record TaskUpdateRequest(
        String title,
        String description,
        Boolean completed,
        TaskStatus status,
        OffsetDateTime dueDate
) {}