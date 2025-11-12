package com.smarttask.smarttask_backend.controller;
import com.smarttask.smarttask_backend.dto.LoginRequest;
import com.smarttask.smarttask_backend.dto.RegisterRequest;
import com.smarttask.smarttask_backend.dto.TokenResponse;
import com.smarttask.smarttask_backend.dto.UserResponse;
import com.smarttask.smarttask_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/** WHY: Public auth endpoints for Flutter/web clients. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(auth.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails ud) {
        auth.logout(ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
