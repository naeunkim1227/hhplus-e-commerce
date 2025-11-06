package io.hhplus.ecommerce.cart.presentation.dto.request;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장바구니 수량 변경 요청 DTO (Presentation Layer)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDeleteRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long cartItemId;

    public CartItemDeleteCommand toCommand() {
        return CartItemDeleteCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .build();
    }
}