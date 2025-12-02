package com.cart.controller;

import com.cart.dto.*;
import com.cart.services.CartService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** Add product to cart */
    @PostMapping("/add")
    public GenericResponseDTO<CartResponseDTO> add(@RequestBody AddToCartRequestDTO request) {
        return GenericResponseDTO.<CartResponseDTO>builder()
                .status("SUCCESS")
                .message("Product added to cart")
                .data(cartService.addToCart(request))
                .build();
    }

    /** View cart */
    @GetMapping("/{userId}")
    public GenericResponseDTO<CartResponseDTO> view(@PathVariable String userId) {
        return GenericResponseDTO.<CartResponseDTO>builder()
                .status("SUCCESS")
                .message("Cart fetched")
                .data(cartService.viewCart(userId))
                .build();
    }

    /** Remove product from cart */
    @DeleteMapping("/{userId}/remove/{productId}")
    public GenericResponseDTO<CartResponseDTO> remove(
            @PathVariable String userId,
            @PathVariable String productId) {

        return GenericResponseDTO.<CartResponseDTO>builder()
                .status("SUCCESS")
                .message("Product removed from cart")
                .data(cartService.removeProduct(userId, productId))
                .build();
    }
}
