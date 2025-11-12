package com.smarttask.smarttask_backend.dto;

import java.time.OffsetDateTime;

/**
 * DTO for returning task data to the client.
 * Used as the response type in TaskController.
 */
public record TaskResponse(
        String id,
        String title,
        String description,
        boolean completed,
        OffsetDateTime dueDate,
        String createdAt
) {}
