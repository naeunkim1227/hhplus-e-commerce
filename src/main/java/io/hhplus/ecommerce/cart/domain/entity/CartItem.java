package io.hhplus.ecommerce.cart.domain.entity;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;
    private Long cartItemId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItem create(CartItemAddCommand command) {
        return CartItem.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .quantity(command.getQuantity())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}