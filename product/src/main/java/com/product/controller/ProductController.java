package com.product.controller;

import com.product.dto.GenericResponse;
import com.product.dto.ProductRequestDTO;
import com.product.dto.ProductResponseDTO;
import com.product.services.ProductService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * Requirement:
 * - Search products (wildcard + pagination)
 * - View product detail
 */

@RestController
@Slf4j
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** Search products */
    @GetMapping("/search")
    public GenericResponse<Page<ProductResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam int page,
            @RequestParam int size) {

        log.info("Search request for keyword: {}", keyword);

        Page<ProductResponseDTO> result = productService.searchProducts(keyword, page, size);

        return GenericResponse.<Page<ProductResponseDTO>>builder()
                .status("SUCCESS")
                .message("Products fetched")
                .data(result)
                .build();
    }

    /** Product detail */
    @GetMapping("/{id}")
    public GenericResponse<ProductResponseDTO> details(@PathVariable String id) {
        log.info("Fetching details for product ID: {}", id);

        return GenericResponse.<ProductResponseDTO>builder()
                .status("SUCCESS")
                .message("Product details fetched")
                .data(productService.getProductDetail(id))
                .build();
    }

    @PostMapping
    public GenericResponse<ProductResponseDTO> create(@RequestBody ProductRequestDTO request) {

        log.info("Create product API invoked");

        return GenericResponse.<ProductResponseDTO>builder()
                .status("SUCCESS")
                .message("Product created successfully")
                .data(productService.createProduct(request))
                .build();
    }

}
