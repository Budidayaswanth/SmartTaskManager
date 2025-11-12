package com.smarttask.smarttask_backend.controller;

import com.smarttask.smarttask_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WHY:
 * - Provides a special login endpoint for Swagger UI (static credentials).
 * - Generates a JWT token that can be used to authorize Swagger API requests.
 * - Useful for testing protected endpoints without registering real users.
 */
@RestController
@RequestMapping("/api/auth/swagger-login")
@RequiredArgsConstructor
public class SwaggerAuthController {

    private final JwtService jwtService;

    @Value("${swagger.auth.username}")
    private String swaggerUsername;

    @Value("${swagger.auth.password}")
    private String swaggerPassword;

    /**
     * POST /api/auth/swagger-login
     *
     * Example Request:
     * {
     *   "username": "swagger-admin",
     *   "password": "swagger@123"
     * }
     *
     * Example Response:
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tokenType": "Bearer"
     * }
     */
    @PostMapping
    public ResponseEntity<?> swaggerLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // Validate credentials against the configured Swagger admin credentials
        if (swaggerUsername.equals(username) && swaggerPassword.equals(password)) {

            // ✅ FIX: pass Map<String, Object> as claims instead of String
            String token = jwtService.generateToken(username, Map.of("role", "SWAGGER_ADMIN"));

            return ResponseEntity.ok(Map.of(
                    "accessToken", token,
                    "tokenType", "Bearer",
                    "message", "Swagger admin login successful"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid Swagger credentials",
                    "message", "Please check your Swagger admin username/password"
            ));
        }
    }
}
