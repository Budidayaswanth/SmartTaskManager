package com.smarttask.smarttask_backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for login requests.
 * Used to pass username/password from client.
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
