package com.cart.servicesImpl;

import com.cart.dto.AddToCartRequestDTO;
import com.cart.dto.CartResponseDTO;
import com.cart.dto.external.ProductDTO;
import com.cart.dto.external.ProductResponseWrapper;
import com.cart.entity.CartEntity;
import com.cart.entity.CartItem;
import com.cart.exception.CartNotFoundException;
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

//    public CartServiceImpl(CartRepository cartRepository,
//                           ProductFeignClient productFeignClient) {
//        this.cartRepository = cartRepository;
//        this.productFeignClient = productFeignClient;
//    }

    /**
     * Requirement:
     * - Add item to cart
     * - Validate product using Feign
     * - If already exists: increase quantity
     */
    @Override
    public CartResponseDTO addToCart(AddToCartRequestDTO request) {

        log.info("Add to cart: user={}, product={}, qty={}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        //Fetch product details from product-service using Feign
        ProductResponseWrapper productWrapper =
                productFeignClient.getProductById(request.getProductId());

        if (productWrapper == null || productWrapper.getData() == null) {
            throw new RuntimeException("Product not found in product-service");
        }

        ProductDTO productData = productWrapper.getData();
        double price = productData.getProductUnitPrice();

        // Fetch existing cart or create new
        CartEntity cart = cartRepository.findById(request.getUserId())
                .orElse(new CartEntity(request.getUserId(), 0, new ArrayList<>()));

        // Check if product exists in cart
        Optional<CartItem> existingItem = cart.getItems()
                .stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Increase quantity
            existingItem.get().setQuantity(
                    existingItem.get().getQuantity() + request.getQuantity()
            );
        } else {
            // Add new item to cart
            cart.getItems().add(new CartItem(productData.getProductId(), request.getQuantity()));
        }

        //Recalculate total using REAL price
        cart.setTotalCartPrice(calculateTotalPrice(cart));

        // Save
        cartRepository.save(cart);

        return convertToDTO(cart);
    }

    /** View cart */
    @Override
    public CartResponseDTO viewCart(String userId) {

        CartEntity cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        return convertToDTO(cart);
    }

    /**
     * Requirement:
     * - Remove one product from cart
     */
    @Override
    public CartResponseDTO removeProduct(String userId, String productId) {

        CartEntity cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        cart.setTotalCartPrice(calculateTotalPrice(cart));

        cartRepository.save(cart);

        return convertToDTO(cart);
    }

    /** Calculates total price on fly by fetching from product via Feign */
    private double calculateTotalPrice(CartEntity cart) {

        double total = 0;

        for (CartItem item : cart.getItems()) {

            // Fetch product price
            ProductResponseWrapper productWrapper =
                    productFeignClient.getProductById(item.getProductId());

            if (productWrapper == null || productWrapper.getData() == null) {
                throw new RuntimeException("Unable to fetch product price");
            }

            double price = productWrapper.getData().getProductUnitPrice();

            total += price * item.getQuantity();
        }

        return total;
    }

    /**  Convert Entity to DTO */
    private CartResponseDTO convertToDTO(CartEntity cart) {
        return CartResponseDTO.builder()
                .userId(cart.getCartId())
                .totalPrice(cart.getTotalCartPrice())
                .items(cart.getItems())
                .build();
    }
}
