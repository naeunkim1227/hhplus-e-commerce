package io.hhplus.ecommerce.order.application.dto.result;


import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 아이템 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private BigDecimal price;
    private LocalDateTime createdAt;

    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .orderId(orderItem.getOrderId())
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .createdAt(orderItem.getCreatedAt())
                .build();
    }
}
