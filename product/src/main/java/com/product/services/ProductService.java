package com.product.services;


import com.product.dto.ProductRequestDTO;
import com.product.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;

public interface ProductService {

        Page<ProductResponseDTO> searchProducts(String keyword, int page, int size);

        ProductResponseDTO getProductDetail(String productId);

       ProductResponseDTO createProduct(ProductRequestDTO request);
    }

