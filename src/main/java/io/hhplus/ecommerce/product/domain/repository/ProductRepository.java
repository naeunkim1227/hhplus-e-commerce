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

    /**
     * 재고 차감 (조건부 UPDATE)
     * @return 업데이트된 행 수 (0이면 재고 부족)
     */
    int decreaseStock(Long productId, int quantity);

    /**
     * 재고 증가 (결제 실패 시 복구용)
     */
    int increaseStock(Long productId, int quantity);
}
