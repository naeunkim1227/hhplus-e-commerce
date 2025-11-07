package io.hhplus.ecommerce.cart.application.dto.result;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.product.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 장바구니 아이템 DTO (Application Layer)
 * CartItem + Product 정보 조합
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    //cart entity
    private Long cartItemId;
    private Long userId;
    private Long productId;
    private int quantity;           // 장바구니에 담은 수량

    //product entity
    private String productName;
    private BigDecimal price;

    private BigDecimal subtotal;

    // 재고 정보
    private Long totalStock;            // 전체 재고
    private Long availableStock;        // 사용 가능한 재고 (전체 - 선점)

    /**
     * CartItem + Product + 재고 정보로부터 DTO 생성
     */
    public static CartItemDto from(CartItem cartItem, Product product, Long availableStock) {
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemDto.builder()
                .cartItemId(cartItem.getId())
                .userId(cartItem.getUserId())
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .totalStock(product.getStock())
                .availableStock(availableStock)
                .build();
    }

    /**
     * CartItem + Product
     */
    public static CartItemDto from(CartItem cartItem, Product product) {
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemDto.builder()
                .cartItemId(cartItem.getId())
                .userId(cartItem.getUserId())
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .build();
    }


    public static CartItemDto from(CartItem cartItem) {
        return CartItemDto.builder()
                .cartItemId(cartItem.getId())
                .userId(cartItem.getUserId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
