package com.cart.dto;
import lombok.Data;

/**
 * Requirement: add product to cart
 */
@Data
public class AddToCartRequestDTO {

    private String userId;
    private String productId;
    private int quantity;
}