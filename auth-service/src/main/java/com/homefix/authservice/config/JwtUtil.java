package com.homefix.authservice.config;

import com.homefix.authservice.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a JWT token with userId, email, and role claims.
     */
    public String generateToken(Long userId, String email, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate a JWT token. Returns true if valid and not expired.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract all claims from a valid token.
     */
    public Claims extractClaims(String token) {
        return parseClaims(token);
    }

    /**
     * Extract the user ID (subject) from the token.
     */
    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    /**
     * Extract the email claim from the token.
     */
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    /**
     * Extract the role claim from the token.
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
