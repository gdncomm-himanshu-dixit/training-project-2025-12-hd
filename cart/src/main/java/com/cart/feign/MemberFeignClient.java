package com.cart.feign;


import com.cart.dto.external.ExistsResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member", url = "http://localhost:8093")
public interface MemberFeignClient {

    @GetMapping("/api/v1/member/exists/{id}")
    ExistsResponseDTO existsById(@PathVariable("id") String id);
}