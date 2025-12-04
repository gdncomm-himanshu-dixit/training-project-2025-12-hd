package com.cart.servicesImpl;

import com.cart.dto.AddToCartRequestDTO;
import com.cart.dto.CartResponseDTO;
import com.cart.dto.external.ProductDTO;
import com.cart.dto.external.ProductResponseWrapper;
import com.cart.dto.external.ExistsResponseDTO;
import com.cart.entity.CartEntity;
import com.cart.entity.CartItem;
import com.cart.exception.CartNotFoundException;
import com.cart.feign.MemberFeignClient;
import com.cart.feign.ProductFeignClient;
import com.cart.repositories.CartRepository;
import com.cart.services.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductFeignClient productFeignClient;
    private final MemberFeignClient memberFeignClient;

    // -------------------------------------------------------
    // ADD TO CART
    // -------------------------------------------------------
    @Override
    public CartResponseDTO addToCart(String userId, AddToCartRequestDTO request) {

        log.info("Add to cart: user={}, product={}, qty={}",
                userId, request.getProductId(), request.getQuantity());

        // Validate user exists
        validateUserExists(userId);

        // Fetch product details
        ProductResponseWrapper productWrapper =
                productFeignClient.getProductById(request.getProductId());

        if (productWrapper == null || productWrapper.getData() == null) {
            throw new RuntimeException("Product not found in product-service");
        }

        ProductDTO product = productWrapper.getData();

        // Load or create cart
        CartEntity cart = cartRepository.findById(userId)
                .orElse(new CartEntity(userId, 0, new ArrayList<>()));

        // Check if product already exists
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(
                    existing.get().getQuantity() + request.getQuantity()
            );
        } else {
            cart.getItems().add(new CartItem(product.getProductId(), request.getQuantity()));
        }

        // Recalculate cart total
        cart.setTotalCartPrice(calculateTotalPrice(cart));

        cartRepository.save(cart);

        return convertToDTO(cart);
    }

    // -------------------------------------------------------
    // VIEW CART
    // -------------------------------------------------------
    @Override
    public CartResponseDTO viewCart(String userId) {

        log.info("Fetching cart for user {}", userId);

        validateUserExists(userId);

        CartEntity cart = cartRepository.findById(userId)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found for user: " + userId)
                );

        return convertToDTO(cart);
    }

    // -------------------------------------------------------
    // REMOVE PRODUCT
    // -------------------------------------------------------
    @Override
    public CartResponseDTO removeProduct(String userId, String productId) {

        log.info("Removing product {} from cart of user {}", productId, userId);

        validateUserExists(userId);

        CartEntity cart = cartRepository.findById(userId)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found for user: " + userId)
                );

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            log.warn("Product {} not found in cart for user {}", productId, userId);
        }

        cart.setTotalCartPrice(calculateTotalPrice(cart));
        cartRepository.save(cart);

        return convertToDTO(cart);
    }

    // -------------------------------------------------------
    // HELPER: Validate User Exists
    // -------------------------------------------------------
    private void validateUserExists(String userId) {

        ExistsResponseDTO response = memberFeignClient.existsById(userId);

        boolean exists = response != null && Boolean.TRUE.equals(response.getData());

        if (!exists) {
            log.error("User {} does NOT exist in member-service", userId);
            throw new RuntimeException("User does not exist");
        }
    }

    // -------------------------------------------------------
    // HELPER: Calculate Total Price (always fresh from product-service)
    // -------------------------------------------------------
    private double calculateTotalPrice(CartEntity cart) {

        double total = 0;

        for (CartItem item : cart.getItems()) {

            ProductResponseWrapper wrapper =
                    productFeignClient.getProductById(item.getProductId());

            if (wrapper == null || wrapper.getData() == null) {
                throw new RuntimeException("Unable to fetch product price for cart calculation");
            }

            double price = wrapper.getData().getProductUnitPrice();
            total += price * item.getQuantity();
        }

        return total;
    }

    // -------------------------------------------------------
    // HELPER: Convert Entity → DTO
    // -------------------------------------------------------
    private CartResponseDTO convertToDTO(CartEntity cart) {

        return CartResponseDTO.builder()
                .userId(cart.getCartId())
                .totalPrice(cart.getTotalCartPrice())
                .items(cart.getItems())
                .build();
    }
}
