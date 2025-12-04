package com.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @PostMapping("/logout")
    public Mono<String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just("Missing token").thenReturn("ERROR: Missing Authorization header");
        }

        String token = authHeader.substring(7);
        String blacklistKey = "BLACKLIST:" + token;

        log.info(">>> Logging out token: {}", token);

        // Store token in blacklist with expiration (same as token expiry)
        return redisTemplate
                .opsForValue()
                .set(blacklistKey, "true", Duration.ofHours(2))
                .thenReturn("SUCCESS: Logged out");
    }
}
