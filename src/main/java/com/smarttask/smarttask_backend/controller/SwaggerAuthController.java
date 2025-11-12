package com.smarttask.smarttask_backend.controller;

import com.smarttask.smarttask_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Allows Swagger UI to authenticate using static credentials.
 * Generates a JWT token for Swagger testing.
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

    @PostMapping
    public ResponseEntity<?> swaggerLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (swaggerUsername.equals(username) && swaggerPassword.equals(password)) {
            // ✅ Pass claims properly (Map<String, Object>)
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
