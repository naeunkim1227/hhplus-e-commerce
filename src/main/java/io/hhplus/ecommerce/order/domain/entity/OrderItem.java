package io.hhplus.ecommerce.order.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public static OrderItem create(
            Long orderId, Long productId,BigDecimal price, int quantity
    ) {
        return OrderItem.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .price(price)  // 구매 시점의 상품 가격 저장
                .createdAt(LocalDateTime.now())
                .build();
    }
}