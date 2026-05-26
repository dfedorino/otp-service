package com.dfedorino.otp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.dfedorino.otp.domain.enums.Role;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtService {
    private final SecretKey key;
    private final long expirationMilliseconds;

    public JwtService(String secret, long expirationSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                "JWT secret must be at least 32 characters"
            );
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMilliseconds = expirationSeconds * 1000;
    }

    public String generateToken(long userId, String username, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMilliseconds);
        
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Role extractRole(String token) {
        String roleName = extractClaims(token).get("role", String.class);
        return Role.valueOf(roleName);
    }

    public long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}