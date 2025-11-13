package io.hhplus.ecommerce.product.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaProductRepository extends JpaRepository<Product, Long> {

    /**
     * 최근 N일 동안 가장 판매량이 많은 상품 조회 (네이티브 쿼리)
     */
    @Query(value = """
        SELECT p.*
        FROM products p
        INNER JOIN order_items oi ON p.id = oi.product_id
        INNER JOIN orders o ON oi.order_id = o.id
        WHERE o.status = 'PAYMENT_COMPLETED'
          AND o.ordered_at >= :startDate
        GROUP BY p.id
        ORDER BY SUM(oi.quantity) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findPopularProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("limit") int limit
    );
}