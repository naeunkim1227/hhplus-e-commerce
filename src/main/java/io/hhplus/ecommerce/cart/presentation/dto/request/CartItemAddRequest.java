package io.hhplus.ecommerce.cart.presentation.dto.request;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장바구니 담기 요청 DTO (Presentation Layer)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private int quantity;

    public CartItemAddCommand toCommand() {
        return CartItemAddCommand.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}