package io.hhplus.ecommerce.cart.presentation.dto.response;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 장바구니 아이템 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    //dto > response
    public static CartItemResponse from(CartItemDto dto) {
        return CartItemResponse.builder()
                .cartItemId(dto.getCartItemId())
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .subtotal(dto.getSubtotal())
                .build();
    }
}
