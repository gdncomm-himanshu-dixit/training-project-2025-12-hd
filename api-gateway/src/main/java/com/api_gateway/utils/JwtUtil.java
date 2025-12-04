package com.api_gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private static final String SECRET = "THIS_IS_A_256_BIT_SECRET_KEY_FOR_JWT_TOKEN_XYZ_1234567890";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /** Extract userId claim */
    public String extractUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

    /** Extract username (subject) */
    public String extractUserName(String token) {
        return getClaims(token).getSubject();
    }

    /** Validate token */
    public void validateToken(String token) {
        getClaims(token);
    }

    /** Internal claim parser */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
