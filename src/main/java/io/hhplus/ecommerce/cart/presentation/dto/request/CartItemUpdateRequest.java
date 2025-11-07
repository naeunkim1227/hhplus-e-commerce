package io.hhplus.ecommerce.cart.presentation.dto.request;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import jakarta.validation.constraints.Min;
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
public class CartItemUpdateRequest {
    @NotNull
    private Long userId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;

    @NotNull
    private Long cartItemId;

    public CartItemUpdateCommand toCommand() {
        return CartItemUpdateCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(quantity)
                .build();
    }
}