package io.hhplus.ecommerce.fixture;

import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order 엔티티 테스트 픽스처
 */
public class OrderFixture {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 주문 생성 (PENDING 상태)
     */
    public static Order defaultOrder() {
        return Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .couponId(null)
                .totalAmount(BigDecimal.valueOf(100000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(100000))
                .createdAt(DEFAULT_TIMESTAMP)
                .orderedAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 쿠폰이 적용된 주문 생성
     */
    public static Order orderWithCoupon() {
        return Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .couponId(1L)
                .totalAmount(BigDecimal.valueOf(100000))
                .discountAmount(BigDecimal.valueOf(10000))
                .finalAmount(BigDecimal.valueOf(90000))
                .createdAt(DEFAULT_TIMESTAMP)
                .orderedAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 완료된 주문 생성
     */
    public static Order completedOrder() {
        return Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.COMPLETED)
                .couponId(null)
                .totalAmount(BigDecimal.valueOf(100000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(100000))
                .createdAt(DEFAULT_TIMESTAMP)
                .orderedAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }
}