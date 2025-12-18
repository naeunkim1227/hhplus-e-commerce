package io.hhplus.ecommerce.cart.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartAddedEvent {
    private Long userId;
    private Long productId;
    private int quantity;
}
