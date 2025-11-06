package io.hhplus.ecommerce.order.domain.entity;

import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private Long couponId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;

    public static Order create(
            Long orderId,
            Long userId,
            Long couponId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        return Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .couponId(couponId)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }


    public void changeStatus(OrderStatus status){
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}