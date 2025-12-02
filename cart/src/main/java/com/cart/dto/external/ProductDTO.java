package com.cart.dto.external;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private String productId;
    private String productName;
    private String productDesc;
    private Double productUnitPrice;
    private String category;
    private List<String> images;
}
