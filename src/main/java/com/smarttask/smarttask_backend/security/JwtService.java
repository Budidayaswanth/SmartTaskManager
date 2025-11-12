package com.smarttask.smarttask_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * WHY:
 * - Centralized JWT utility for token generation, validation, and parsing.
 * - Reads configuration values from application.yml (security.jwt.*)
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessExpMin;
    private final long refreshExpDays;

    public JwtService(
            @Value("${security.jwt.secret}") String rawSecret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-exp-min}") long accessExpMin,
            @Value("${security.jwt.refresh-exp-days}") long refreshExpDays
    ) {
        try {
            if (rawSecret == null || rawSecret.isBlank()) {
                throw new IllegalArgumentException("JWT_SECRET is missing or empty!");
            }

            // ✅ Clean the secret to remove whitespace or newline characters
            String cleanSecret = rawSecret.replaceAll("\\s+", "");

            this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(cleanSecret));
            this.issuer = issuer;
            this.accessExpMin = accessExpMin;
            this.refreshExpDays = refreshExpDays;

            System.out.println("✅ JwtService initialized — issuer: " + issuer);
        } catch (Exception e) {
            System.err.println("❌ Error initializing JwtService — check your JWT_SECRET format!");
            throw new RuntimeException("Invalid JWT_SECRET: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------
    // ✅ 1. Generate Access Token with Claims
    // ---------------------------------------------------------------------
    public String generateToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(accessExpMin * 60)))
                .signWith(key)
                .compact();
    }

    // ---------------------------------------------------------------------
    // ✅ 2. Generate Refresh Token
    // ---------------------------------------------------------------------
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(refreshExpDays * 24 * 60 * 60)))
                .signWith(key)
                .compact();
    }

    // ---------------------------------------------------------------------
    // ✅ 3. Extract Subject (username/email)
    // ---------------------------------------------------------------------
    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ---------------------------------------------------------------------
    // ✅ 4. Validate Token
    // ---------------------------------------------------------------------
    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails user) {
        String username = getSubject(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    private Date getExpiration(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}
