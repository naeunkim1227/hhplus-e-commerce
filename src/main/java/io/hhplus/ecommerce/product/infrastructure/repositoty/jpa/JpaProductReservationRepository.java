package io.hhplus.ecommerce.product.infrastructure.repositoty.jpa;

import io.hhplus.ecommerce.product.domain.entity.ProductReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaProductReservationRepository extends JpaRepository<ProductReservation, Long> {
    Optional<ProductReservation> findByOrderId(Long orderId);

    @Query("SELECT pr FROM ProductReservation pr WHERE pr.productId = :productId AND pr.status = 'ACTIVE'")
    List<ProductReservation> findActiveReservationsByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(pr.quantity), 0) FROM ProductReservation pr WHERE pr.productId = :productId AND pr.status = 'ACTIVE'")
    Long getTotalReservedQuantity(@Param("productId") Long productId);

    @Query("SELECT pr FROM ProductReservation pr WHERE pr.status = 'ACTIVE' AND pr.expiredAt < :now")
    List<ProductReservation> findExpiredReservations(@Param("now") LocalDateTime now);
}