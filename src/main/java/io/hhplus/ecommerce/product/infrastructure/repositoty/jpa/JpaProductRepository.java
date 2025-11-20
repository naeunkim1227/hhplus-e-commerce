package io.hhplus.ecommerce.product.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        INNER JOIN orders o ON oi.order_id = o.id
        INNER JOIN order_items oi ON p.id = oi.product_id
        WHERE  o.ordered_at >= :startDate
          AND o.status = 'PAYMENT_COMPLETED'
        GROUP BY p.id
        ORDER BY SUM(oi.quantity) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findPopularProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("limit") int limit
    );

    /**
     * 재고 차감 (조건부 업데이트)
     * stock >= quantity 조건으로 동시성 제어
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " +
           "WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId,
                      @Param("quantity") int quantity);

    /**
     * 재고 증가 (결제 실패 시 복구용)
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity " +
           "WHERE p.id = :productId")
    int increaseStock(@Param("productId") Long productId,
                      @Param("quantity") int quantity);
}