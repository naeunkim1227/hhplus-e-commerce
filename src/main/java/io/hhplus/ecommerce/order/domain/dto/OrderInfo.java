package io.hhplus.ecommerce.order.domain.dto;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.product.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {
    Long id;
    Long userId;
    Long couponId;
    List<Product> products;
    List<CartItem> cartItems;
}
