package com.product.servicesImpl;
import com.product.dto.ProductRequestDTO;
import com.product.dto.ProductResponseDTO;
import com.product.entity.ProductEntity;
import com.product.exception.ProductNotFoundException;
import com.product.repositories.ProductRepository;
import com.product.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
        import java.util.stream.Collectors;

/**
 * Req Mapping:
 * - Search product with wildcard
 * - Paginated results
 * - Redis cache for search results (key: search::keyword::page::size)
 * - View product detail
 */
@Slf4j
@Service
@RequiredArgsConstructor // using instead of constructor initialization
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

//    public ProductServiceImpl(ProductRepository productRepository,
//                              RedisTemplate<String, Object> redisTemplate) {
//        this.productRepository = productRepository;
//        this.redisTemplate = redisTemplate;
//    }

    /** Search product with Redis cache + pagination */
    @Override
    public Page<ProductResponseDTO> searchProducts(String keyword, int page, int size) {

        String cacheKey = "product_search_" + keyword + "_" + page + "_" + size;

        // check Redis first
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            log.info("Returning cached results for: {}", cacheKey);
            return (Page<ProductResponseDTO>) redisTemplate.opsForValue().get(cacheKey);
        }

        log.info("Searching products for keyword: {}", keyword);

        List<ProductEntity> productList = productRepository.searchByNameRegex(keyword);

        List<ProductResponseDTO> dtoList = productList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Pagination manually because MongoRepo custom query returns list
        int start = Math.min(page * size, dtoList.size());
        int end = Math.min((page + 1) * size, dtoList.size());
        Page<ProductResponseDTO> pageResult =
                new PageImpl<>(dtoList.subList(start, end), PageRequest.of(page, size), dtoList.size());

        // save to Redis
        redisTemplate.opsForValue().set(cacheKey, pageResult);

        return pageResult;
    }

    /** View product details */
    @Override
    public ProductResponseDTO getProductDetail(String productId) {
        log.info("Fetching product details for ID: {}", productId);

        ProductEntity p = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return convertToDTO(p);
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) {

        log.info("Creating new product: {}", request.getProductName());

        ProductEntity entity = ProductEntity.builder()
                .productName(request.getProductName())
                .productDesc(request.getProductDesc())
                .productUnitPrice(request.getProductUnitPrice())
                .category(request.getCategory())
                .images(List.of()) // optional
                .build();

        ProductEntity saved = productRepository.save(entity);

        return convertToDTO(saved);
    }



    private ProductResponseDTO convertToDTO(ProductEntity p) {
        return ProductResponseDTO.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .productDesc(p.getProductDesc())
                .productUnitPrice(p.getProductUnitPrice())
                .category(p.getCategory())
                .images(p.getImages())
                .build();
    }
}
