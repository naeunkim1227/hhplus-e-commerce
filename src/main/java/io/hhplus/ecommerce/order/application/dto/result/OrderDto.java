package io.hhplus.ecommerce.order.application.dto.result;


import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private String status;
    private Long couponId;
    private Double totalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private Long createdAt;
    private Long updatedAt;
    private List<OrderItemDto> orderItems;

    public static OrderDto from(Order order, List<OrderItem> orderItems) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .couponId(order.getCouponId())
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : null)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : null)
                .finalAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : null)
                .orderItems(orderItems.stream()
                        .map(OrderItemDto::from)
                        .toList())
                .build();
    }


    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .couponId(order.getCouponId())
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : null)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : null)
                .finalAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : null)
                .orderItems(null) // 아예 안채움
                .build();
    }
}
