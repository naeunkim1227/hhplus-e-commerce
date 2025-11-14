package io.hhplus.ecommerce.cart.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CartItemAddCommand {
    private Long userId;
    private Long productId;
    private int quantity;
}