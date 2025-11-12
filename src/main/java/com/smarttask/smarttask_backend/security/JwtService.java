package com.smarttask.smarttask_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.access-exp-min}")
    private long accessExpMin;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /** ✅ Generate a simple token (no custom claims) */
    public String generateToken(String username, String swaggerAdmin) {
        return generateAccessToken(username, Map.of());
    }

    /** ✅ Generate a token with optional custom claims (used for Swagger login etc.) */
    public String generateAccessToken(String username, Map<String, String> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpMin * 60)))
                .issuer(issuer)
                .signWith(key)
                .compact();
    }

    /** ✅ Extract subject (username) from token */
    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** ✅ Validate a token against a user */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getSubject(token);
        return username.equals(userDetails.getUsername());
    }
}
