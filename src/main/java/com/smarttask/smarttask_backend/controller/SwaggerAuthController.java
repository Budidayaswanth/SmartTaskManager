package com.smarttask.smarttask_backend.controller;

import com.smarttask.smarttask_backend.security.JwtService;
import com.smarttask.smarttask_backend.dto.SwaggerLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/swagger-login")
@RequiredArgsConstructor
public class SwaggerAuthController {

    private final JwtService jwtService;

    @Value("${swagger.auth.username}")
    private String swaggerUsername;

    @Value("${swagger.auth.password}")
    private String swaggerPassword;

    @Operation(summary = "Swagger Admin Login",
            description = "Authenticate Swagger UI and get a Bearer token for testing APIs.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"<jwt_token>\", \"tokenType\": \"Bearer\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping
    public ResponseEntity<?> swaggerLogin(@RequestBody @Valid SwaggerLoginRequest request) {
        if (swaggerUsername.equals(request.getUsername()) && swaggerPassword.equals(request.getPassword())) {
            String token = jwtService.generateToken(request.getUsername(), Map.of("role", "SWAGGER_ADMIN"));
            return ResponseEntity.ok(Map.of(
                    "accessToken", token,
                    "tokenType", "Bearer",
                    "message", "Swagger admin login successful"
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid Swagger credentials"));
    }

}
