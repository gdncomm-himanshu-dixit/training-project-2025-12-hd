package com.cart.controller;

import com.cart.dto.*;
import com.cart.services.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** Add product to cart — userId injected securely from Gateway */
    @PostMapping("/add")
    public GenericResponseDTO<CartResponseDTO> add(
            @RequestHeader(value = "X-USERID", required = false) String userId,
            @RequestBody AddToCartRequestDTO request) {

        if (userId == null || userId.isBlank()) {
            return GenericResponseDTO.<CartResponseDTO>builder()
                    .status("ERROR")
                    .message("Unauthorized - Missing X-USERID")
                    .data(null)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }

        return GenericResponseDTO.<CartResponseDTO>builder()
                .status("SUCCESS")
                .message("Product added to cart")
                .data(cartService.addToCart(userId, request))
                .statusCode(HttpStatus.OK.value())
                .build();
    }


    /** View cart — requires secure user header */
    @GetMapping("/view")
    public GenericResponseDTO<CartResponseDTO> viewCart(
            @RequestHeader(value = "X-USERID", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return GenericResponseDTO.<CartResponseDTO>builder()
                    .status("ERROR")
                    .message("Unauthorized - Missing X-USERID")
                    .data(null)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }

        return GenericResponseDTO.<CartResponseDTO>builder()
                .status("SUCCESS")
                .message("Cart fetched successfully")
                .data(cartService.viewCart(userId))
                .statusCode(HttpStatus.OK.value())
                .build();
    }


    /** Remove product from cart — userId from gateway header */
    @DeleteMapping("/remove/{productId}")
    public GenericResponseDTO<CartResponseDTO> remove(
            @RequestHeader(value = "X-USERID", required = false) String userId,
            @PathVariable String productId) {

        if (userId == null || userId.isBlank()) {
            return GenericResponseDTO.<CartResponseDTO>builder()
                    .status("ERROR")
                    .message("Unauthorized - Missing X-USERID")
                    .data(null)
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build();
        }

        return GenericResponseDTO.<CartResponseDTO>builder()
                .status("SUCCESS")
                .message("Product removed from cart")
                .data(cartService.removeProduct(userId, productId))
                .statusCode(HttpStatus.OK.value())
                .build();
    }
}
