package io.hhplus.ecommerce.cart.infrastructure.jpa;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.vo.CartItemWithProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByIdIn(List<Long> ids);

    List<CartItem> findByUserId(Long userId);

    /**
     * 장바구니와 상품을 JOIN하여 조회 (1번 쿼리)
     */
    @Query("""
        SELECT new io.hhplus.ecommerce.cart.domain.vo.CartItemWithProduct(
            ci.id,
            ci.userId,
            ci.productId,
            ci.quantity,
            ci.createdAt,
            ci.updatedAt,
            p.name,
            p.price,
            p.stock
        )
        FROM CartItem ci
        INNER JOIN Product p ON ci.productId = p.id
        WHERE ci.userId = :userId
        """)
    List<CartItemWithProduct> findByUserIdWithProduct(@Param("userId") Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserId(Long userId);
}