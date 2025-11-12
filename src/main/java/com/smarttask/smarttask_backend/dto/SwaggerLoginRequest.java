package com.smarttask.smarttask_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SwaggerLoginRequest {

    @Schema(example = "swagger-admin", description = "Swagger admin username")
    private String username;

    @Schema(example = "swagger@123", description = "Swagger admin password")
    private String password;
}
