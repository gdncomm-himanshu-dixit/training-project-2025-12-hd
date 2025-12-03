package com.member.feign;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "gateway-service", url = "http://localhost:8094")
public interface GatewayFeignClient {
    // JWT validation, logout handled here later
}