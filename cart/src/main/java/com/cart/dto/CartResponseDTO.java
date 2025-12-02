package com.cart.dto;
import com.cart.entity.CartItem;
import lombok.*;

import java.util.List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class CartResponseDTO {

        private String userId;
        private double totalPrice;
        private List<CartItem> items;
    }
