package com.cart.dto.external;

import lombok.Data;

@Data
public class ProductResponseWrapper {
    private String status;
    private String message;
    private ProductDTO data;
}
