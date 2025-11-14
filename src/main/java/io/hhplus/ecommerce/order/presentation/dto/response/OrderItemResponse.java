package io.hhplus.ecommerce.order.presentation.dto.response;

import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주문 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long orderId;
    private Long productId;
    private int quantity;
    private BigDecimal price;

    public static OrderItemResponse from(OrderDto dto) {
        return OrderItemResponse.builder()

                .build();
    }
}