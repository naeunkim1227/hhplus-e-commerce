package io.hhplus.ecommerce.product.domain.repository;

import io.hhplus.ecommerce.product.domain.entity.ProductReservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상품 재고 선점 Repository
 */
public interface ProductReservationRepository {

    /**
     * 재고 선점 저장
     */
    ProductReservation save(ProductReservation reservation);

    /**
     * ID로 선점 조회
     */
    Optional<ProductReservation> findById(Long id);

    /**
     * 유저 ID로 선점 조회
     */
    Optional<ProductReservation> findByOrderId(Long orderId);

    /**
     * 특정 상품의 활성(만료되지 않은) 선점 목록
     */
    List<ProductReservation> findActiveReservationsByProductId(Long productId);

    /**
     * 특정 상품의 활성 선점 총 수량 계산
     */
    Long getTotalReservedQuantity(Long productId);

    /**
     * 만료된 선점 조회
     */
    List<ProductReservation> findExpiredReservations(LocalDateTime now);

    /**
     * 선점 삭제
     */
    void deleteById(Long id);
}