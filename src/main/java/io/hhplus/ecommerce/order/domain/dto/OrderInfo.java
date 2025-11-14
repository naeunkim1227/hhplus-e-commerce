package io.hhplus.ecommerce.order.domain.dto;

import io.hhplus.ecommerce.product.domain.entity.Product;

import java.math.BigDecimal;
import java.util.List;


public record OrderInfo (
    Long orderId,
    Long userId,
    Long couponId,
    List<OrderItemInfo> orderItems,
    BigDecimal discountAmount
){

    public static OrderInfo from(Long orderId,Long userId, Long couponId, List<OrderItemInfo> orderItems, BigDecimal discountAmount) {
        return new OrderInfo(
                orderId,
                userId,
                couponId,
                orderItems,
                discountAmount
        );
    }

}
