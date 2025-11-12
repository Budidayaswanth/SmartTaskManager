package com.smarttask.smarttask_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user registration request.
 * Used in AuthController for /register.
 */
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password
) {}