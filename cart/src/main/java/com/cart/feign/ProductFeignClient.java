package com.cart.feign;

import com.cart.dto.external.ProductResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
    /**
     * Requirement:
     * - Validate product existence via Product Service
     */

    @FeignClient(name = "product", url = "http://localhost:8091")
    public interface ProductFeignClient {

        @GetMapping("/api/v1/product/{id}")
//        String getProductById(@PathVariable("id") String productId);
        ProductResponseWrapper getProductById(@PathVariable("id") String id);

    }



