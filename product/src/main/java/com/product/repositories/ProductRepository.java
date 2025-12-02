package com.product.repositories;

import com.product.entity.ProductEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {

    /** Requirement:
     * Wildcard search using regex
     */
    @Query("{ 'productName': { $regex: ?0, $options: 'i' } }")
    List<ProductEntity> searchByNameRegex(String regex);
}