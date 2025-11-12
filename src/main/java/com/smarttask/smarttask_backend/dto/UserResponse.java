package com.smarttask.smarttask_backend.dto;

public record UserResponse(
        String id,
        String username,
        String email,
        String role
) {}