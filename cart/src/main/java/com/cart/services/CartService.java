package com.cart.services;

import com.cart.dto.AddToCartRequestDTO;
import com.cart.dto.CartResponseDTO;

public interface CartService {

    // Add or update quantity for a product
    CartResponseDTO addToCart(AddToCartRequestDTO request);

    // Get all items for a user
    CartResponseDTO viewCart(String userId);

    // Remove a product from cart
    CartResponseDTO removeProduct(String userId, String productId);
}
