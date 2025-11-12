package com.smarttask.smarttask_backend.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}
