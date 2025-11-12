package com.smarttask.smarttask_backend.controller;

import com.smarttask.smarttask_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Allows Swagger UI to authenticate using static credentials.
 * Use this to get a JWT token for testing in Swagger.
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
            String token = jwtService.generateToken(username, "SWAGGER_ADMIN");
            return ResponseEntity.ok(Map.of("accessToken", token, "tokenType", "Bearer"));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Swagger credentials"));
        }
    }
}
