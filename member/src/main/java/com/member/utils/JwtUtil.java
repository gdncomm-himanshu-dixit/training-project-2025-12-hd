package com.member.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final String SECRET =
            "THIS_IS_A_256_BIT_SECRET_KEY_FOR_JWT_TOKEN_XYZ_1234567890";

    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 2; // 2 hours

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /** Generate JWT with username + userId */
    public String generateToken(String userName, UUID userId) {
        return Jwts.builder()
                .setSubject(userName)                     // subject = username
                .claim("userId", userId.toString())       // custom claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Validate signature + expiry */
    public void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }

    /** Extract username from token */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /** NEW → Extract userId from token */
    public String extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", String.class);  // return UUID as string
    }
}
