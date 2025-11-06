package io.hhplus.ecommerce.order.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 엔티티 단위 테스트")
class OrderTest {

    @Test
    @DisplayName("유효한 값으로 주문을 생성하면 성공한다")
    void order_Success() {
        // Given
        Long orderId = 1L;
        Long userId = 1L;
        Long couponId = 10L;
        BigDecimal totalAmount = new BigDecimal("100000");
        BigDecimal discountAmount = new BigDecimal("10000");
        BigDecimal finalAmount = new BigDecimal("90000");

        // When
        Order order = Order.create(orderId, userId, couponId, totalAmount, discountAmount, finalAmount);

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getCouponId()).isEqualTo(couponId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(totalAmount);
        assertThat(order.getDiscountAmount()).isEqualByComparingTo(discountAmount);
        assertThat(order.getFinalAmount()).isEqualByComparingTo(finalAmount);
        assertThat(order.getOrderedAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 상태 변경")
    void changeStatus_Success() {
        // Given
        Order order = Order.create(1L, 1L, null,
            BigDecimal.valueOf(50000), BigDecimal.ZERO, BigDecimal.valueOf(50000));

        // When
        order.changeStatus(OrderStatus.COMPLETED);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getUpdatedAt()).isNotNull();
    }
}