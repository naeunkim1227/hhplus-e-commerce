package io.hhplus.ecommerce.order.application.dto.command;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.product.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 커맨드 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateFromCartCommand {
    private Long userId;
    private Long couponId;
    private List<Long> cartItemIds;

    public static OrderInfo of(Long id,Long userId, Long couponId, List<Product> products, List<CartItem>cartItems) {
        return OrderInfo.builder()
                .id(id)
                .userId(userId)
                .couponId(couponId)
                .cartItems(cartItems)
                .products(products)
                .build();
    }

}