package com.api_gateway.filter;

import com.api_gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    // Public URLs (no JWT needed)
    private static final String[] PUBLIC_URLS = {
            "/api/v1/product",
            "/api/v1/member/register",
            "/api/v1/member/login"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info(">>> JWT FILTER PATH: {}", path);

        // Skip token validation for public URLs
        if (isPublicUrl(path)) {
            log.info(">>> PUBLIC URL: Skipping JWT validation");
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // ----------------------------------
        // 🔥 STEP 1 — CHECK REDIS BLACKLIST
        // ----------------------------------
        String blacklistKey = "BLACKLIST:" + token;

        return redisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn(">>> TOKEN BLACKLISTED — Access denied");
                        return unauthorized(exchange, "Token expired or logged out");
                    }

                    // Continue validation if not blacklisted
                    return validateAndForward(exchange, chain, token);
                });
    }

    // Perform JWT validation and forward request
    private Mono<Void> validateAndForward(ServerWebExchange exchange,
                                          org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
                                          String token) {

        String username;
        String userId;

        try {
            jwtUtil.validateToken(token);
            username = jwtUtil.extractUserName(token);
            userId = jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.error("JWT VALIDATION FAILED: {}", e.getMessage());
            return unauthorized(exchange, "Invalid JWT Token");
        }

        // Remove spoofed headers
        ServerHttpRequest.Builder mutated = exchange.getRequest().mutate();
        mutated.headers(h -> {
            h.remove("X-USERID");
            h.remove("X-USERNAME");
        });

        // Inject validated identity
        mutated.header("X-USERID", userId);
        mutated.header("X-USERNAME", username);

        log.info(">>> AUTH OK: username={}, userId={}", username, userId);

        return chain.filter(exchange.mutate().request(mutated.build()).build());
    }

    // Public URL matcher
    private boolean isPublicUrl(String path) {
        for (String open : PUBLIC_URLS) {
            if (path.startsWith(open))
                return true;
        }
        return false;
    }

    // Unauthorized helper
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn(">>> UNAUTHORIZED: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
