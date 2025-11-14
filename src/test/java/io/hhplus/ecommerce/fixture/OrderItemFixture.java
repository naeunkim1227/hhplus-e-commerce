package io.hhplus.ecommerce.fixture;

import io.hhplus.ecommerce.order.domain.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem 엔티티 테스트 픽스처
 */
public class OrderItemFixture {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 주문 아이템 생성
     */
    public static OrderItem defaultOrderItem() {
        return OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .quantity(2)
                .price(BigDecimal.valueOf(50000))
                .createdAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 커스터마이즈 가능한 주문 아이템 생성
     */
    public static OrderItem createOrderItem(Long orderId, Long productId, int quantity, BigDecimal price) {
        return OrderItem.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .createdAt(DEFAULT_TIMESTAMP)
                .build();
    }
}