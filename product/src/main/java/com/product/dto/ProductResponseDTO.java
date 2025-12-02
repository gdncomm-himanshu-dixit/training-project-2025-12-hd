package com.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductResponseDTO implements Serializable {
    private String productId;
    private String productName;
    private String productDesc;
    private Double productUnitPrice;
    private String category;
    private List<String> images;
}
