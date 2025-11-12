package com.smarttask.smarttask_backend.security;

import io.jsonwebtoken.Claims;
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
 * JWT Utility Service for token generation and validation.
 * Handles both app user tokens and Swagger tokens.
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

            // Clean the secret (remove any newlines or spaces)
            String cleanSecret = rawSecret.replaceAll("\\s+", "");
            this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(cleanSecret));
            this.issuer = issuer;
            this.accessExpMin = accessExpMin;
            this.refreshExpDays = refreshExpDays;

            System.out.println("✅ JwtService initialized successfully: issuer=" + issuer);

        } catch (Exception e) {
            throw new RuntimeException("❌ Invalid JWT_SECRET: " + e.getMessage(), e);
        }
    }

    // ✅ Primary access token generator for app users
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(accessExpMin * 60)))
                .signWith(key)
                .compact();
    }

    // ✅ For refresh tokens
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(refreshExpDays * 24 * 60 * 60)))
                .signWith(key)
                .compact();
    }

    // ✅ For Swagger (or any simplified token without user claims)
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

    // ✅ Validation + decoding methods
    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails user) {
        String username = getSubject(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        Date exp = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return exp.before(new Date());
    }
}
