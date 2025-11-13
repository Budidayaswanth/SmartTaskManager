package com.smarttask.smarttask_backend.service;

import com.smarttask.smarttask_backend.dto.LoginRequest;
import com.smarttask.smarttask_backend.dto.RegisterRequest;
import com.smarttask.smarttask_backend.dto.TokenResponse;
import com.smarttask.smarttask_backend.dto.UserResponse;
import com.smarttask.smarttask_backend.entity.RefreshToken;
import com.smarttask.smarttask_backend.entity.User;
import com.smarttask.smarttask_backend.exception.InvalidRefreshTokenException;
import com.smarttask.smarttask_backend.repository.RefreshTokenRepository;
import com.smarttask.smarttask_backend.repository.UserRepository;
import com.smarttask.smarttask_backend.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * WHY:
 * - Business logic for register/login/refresh/logout.
 * - Refresh token rotation for better security.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String username = req.username().trim();
        String email = req.email().trim().toLowerCase();
        String password = req.password();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username, email and password are required");
        }

        if (userRepo.existsByUsername(username)) throw new IllegalArgumentException("Username taken");
        if (userRepo.existsByEmail(email)) throw new IllegalArgumentException("Email taken");

        var u = User.builder()
                .username(username)
                .email(email)
                .password(encoder.encode(password))
                .role("USER")
                .enabled(true)
                .deleted(false)
                .build();
        userRepo.save(u);
        return new UserResponse(u.getId().toString(), u.getUsername(), u.getEmail(), u.getRole());
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        String username = req.username().trim();
        String rawPassword = req.password();

        var u = userRepo.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (u.isDeleted()) {
            throw new DisabledException("Account has been removed");
        }
        if (!u.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        if (!encoder.matches(rawPassword, u.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String access = jwtService.generateAccessToken(u.getUsername(), Map.of(
                "role", u.getRole(),
                "uid", u.getId().toString()
        ));
        String refresh = rotateRefresh(u);
        return new TokenResponse(access, refresh, "Bearer");
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }
        var saved = rtRepo.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        if (saved.getExpiresAt().isBefore(Instant.now())) {
            saved.setRevoked(true);
            rtRepo.save(saved);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        var u = saved.getUser();
        saved.setRevoked(true);            // revoke old
        rtRepo.save(saved);

        String access = jwtService.generateAccessToken(u.getUsername(), Map.of(
                "role", u.getRole(),
                "uid", u.getId().toString()
        ));
        String refresh = rotateRefresh(u); // issue new refresh
        return new TokenResponse(access, refresh, "Bearer");
    }

    @Transactional
    public void logout(String username) {
        userRepo.findByUsernameAndDeletedFalse(username)
                .ifPresent(user -> rtRepo.deleteByUser_Id(user.getId())); // revoke all refresh tokens
    }

    private String rotateRefresh(User u) {
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        var rt = RefreshToken.builder()
                .user(u)
                .token(token)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        rtRepo.save(rt);
        return token;
    }
}
