package com.smarttask.smarttask_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessExpMin;
    private final long refreshExpDays;

    /**
     * WHY:
     * - Reads JWT config from application.yml (security.jwt.*)
     * - Initializes an HMAC-SHA key for token signing
     * - Handles bad secrets gracefully so Render deployment doesn't crash
     */
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

            // ✅ Trim all whitespace/newlines before decoding
            String cleanSecret = rawSecret.replaceAll("\\s+", "");

            this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(cleanSecret));
            this.issuer = issuer;
            this.accessExpMin = accessExpMin;
            this.refreshExpDays = refreshExpDays;

            System.out.println("✅ JWT Service initialized successfully — issuer: " + issuer);

        } catch (Exception e) {
            System.err.println("❌ ERROR initializing JwtService — invalid JWT_SECRET format!");
            throw new RuntimeException("Invalid JWT_SECRET: " + e.getMessage(), e);
        }
    }

    // ---------------- Token Generation ---------------- //

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

    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(refreshExpDays * 24 * 60 * 60)))
                .signWith(key)
                .compact();
    }

    // ---------------- Token Validation ---------------- //

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails user) {
        String username = getSubject(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
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
