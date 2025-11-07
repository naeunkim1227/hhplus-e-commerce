package io.hhplus.ecommerce.cart.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CartItemDeleteCommand {
    private Long userId;
    private Long cartItemId;
}