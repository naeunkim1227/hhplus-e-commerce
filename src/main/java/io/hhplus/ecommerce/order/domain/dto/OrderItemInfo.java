package io.hhplus.ecommerce.order.domain.dto;

import io.hhplus.ecommerce.product.domain.entity.Product;

import java.math.BigDecimal;

public record OrderItemInfo(
    Long productId,
    BigDecimal price,
    int quantity
){

    public static OrderItemInfo from(Product product, int quantity) {
        return new OrderItemInfo(
                product.getId(),
                product.getPrice(),
                quantity
        );
    }

}
