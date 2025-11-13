package io.hhplus.ecommerce.product.domain.repository;

import io.hhplus.ecommerce.product.domain.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAll();
    Product save(Product product);
    List<Product> findAllById(List<Long> productIds);
    List<Product> findPopularProducts(LocalDateTime startDate, int limit);
}
