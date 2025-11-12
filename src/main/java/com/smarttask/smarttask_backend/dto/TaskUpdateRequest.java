package com.smarttask.smarttask_backend.dto;

import java.time.OffsetDateTime;

/**
 * DTO for updating an existing task.
 * All fields optional — only non-null fields will be updated.
 */
public record TaskUpdateRequest(
        String title,
        String description,
        Boolean completed,
        OffsetDateTime dueDate
) {}