package io.hhplus.ecommerce.cart.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 장바구니 아이템 + 상품 정보 조합 VO
 */
@Getter
@AllArgsConstructor
public class CartItemWithProduct {
    // CartItem 정보
    private Long cartItemId;
    private Long userId;
    private Long productId;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Product 정보
    private String productName;
    private BigDecimal price;
    private Long stock;
}