package io.hhplus.ecommerce.order.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem 엔티티 단위 테스트")
class OrderItemTest {

    @Test
    @DisplayName("주문 아이템을 정상적으로 생성한다")
    void create_orderItem_Success() {
        // Given & When
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .orderId(100L)
                .productId(200L)
                .quantity(3)
                .price(new BigDecimal("15000"))
                .createdAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(orderItem).isNotNull();
        assertThat(orderItem.getId()).isEqualTo(1L);
        assertThat(orderItem.getOrderId()).isEqualTo(100L);
        assertThat(orderItem.getProductId()).isEqualTo(200L);
        assertThat(orderItem.getQuantity()).isEqualTo(3);
        assertThat(orderItem.getPrice()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(orderItem.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 아이템의 총 가격을 계산한다")
    void calculate_orderItem_Success() {
        // Given
        OrderItem orderItem = OrderItem.builder()
                .quantity(5)
                .price(new BigDecimal("10000"))
                .build();

        // When
        BigDecimal totalPrice = orderItem.getPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        // Then
        assertThat(totalPrice).isEqualByComparingTo(new BigDecimal("50000"));
    }
}