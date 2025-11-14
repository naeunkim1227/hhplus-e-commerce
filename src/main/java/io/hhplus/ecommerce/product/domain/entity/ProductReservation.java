package io.hhplus.ecommerce.product.domain.entity;

import io.hhplus.ecommerce.product.domain.ProductPolicy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 재고 선점 엔티티
 * 주문 시 재고를 임시로 선점하여 동시성 문제 해결
 */
@Entity
@Table(name = "product_reservations")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private ProductReservationStatus status;
    private LocalDateTime expiredAt;    // 선점 만료 시간
    private LocalDateTime releasedAt;   // 선점 해제 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 재고 선점 생성
     */
    public static ProductReservation create(Long orderId, Long productId, int quantity) {
        LocalDateTime now = LocalDateTime.now();
        return ProductReservation.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .status(ProductReservationStatus.ACTIVE)
                .expiredAt(now.plusMinutes(ProductPolicy.RESERVATION_MINUTES))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }


    /**
     * 선점 상태 변경
     */
    public void changeStatus(ProductReservationStatus status){
        this.status = status;

        if (status == ProductReservationStatus.RELEASED) {
            this.releasedAt = LocalDateTime.now();
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 선점 만료 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * 선점 활성 상태 확인
     */
    public boolean isActive() {
        return status == ProductReservationStatus.ACTIVE && !isExpired();
    }


}

