package com.smarttask.smarttask_backend.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * ✅ JWT service compatible with JJWT 0.12.6
 * Works in Spring Boot 3.5.7, Java 21
 * Requires jjwt-api + jjwt-impl + jjwt-jackson on classpath
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessExpMin;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-exp-min}") long accessExpMin
    ) {
        // This creates an HMAC-SHA key for signing/verification
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessExpMin = accessExpMin;
    }

    /** Generate a JWT access token with custom claims */
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpMin * 60);

        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extract subject (username/email) safely */
    public String getSubject(String token) {
        return Jwts.parser()             // ✅ new API in 0.12.x
                .verifyWith(key)         // ✅ key is already SecretKey
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Extract full claims map (for debugging or auditing) */
    public Map<String, Object> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
