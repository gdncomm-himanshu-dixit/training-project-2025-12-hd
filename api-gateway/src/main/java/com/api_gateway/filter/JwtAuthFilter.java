package com.api_gateway.filter;

import com.api_gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
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

        // Allow public URLs
        if (isPublicUrl(path)) {
            log.info(">>> PUBLIC URL: Skipping JWT validation");
            return chain.filter(exchange);
        }

        // Validate token header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        String username;
        String userId;

        try {
            jwtUtil.validateToken(token);
            username = jwtUtil.extractUserName(token);
            userId = jwtUtil.extractUserId(token);    // <-- FIXED: extract UUID claim
        } catch (Exception e) {
            log.error("JWT VALIDATION FAILED: {}", e.getMessage());
            return unauthorized(exchange, "Invalid JWT Token");
        }

        // Prevent user from spoofing headers
        ServerHttpRequest.Builder mutated = exchange.getRequest().mutate();
        mutated.headers(h -> {
            h.remove("X-USERID");
            h.remove("X-USERNAME");
        });

        // Inject validated identity from JWT
        mutated.header("X-USERID", userId);      // <-- FIXED: Send userId (UUID)
        mutated.header("X-USERNAME", username);  // Optional but useful

        log.info(">>> AUTH: username={}, userId={}", username, userId);

        return chain.filter(exchange.mutate().request(mutated.build()).build());
    }


    private boolean isPublicUrl(String path) {
        for (String url : PUBLIC_URLS) {
            if (path.startsWith(url)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn(">>> UNAUTHORIZED: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
