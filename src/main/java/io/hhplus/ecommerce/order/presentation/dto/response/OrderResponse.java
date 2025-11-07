package io.hhplus.ecommerce.order.presentation.dto.response;

import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String status;
    private Long couponId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime orderedAt;

    public static OrderResponse from(OrderDto dto) {
        return OrderResponse.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .couponId(dto.getCouponId())
                .build();
    }
}