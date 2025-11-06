package io.hhplus.ecommerce.fixture;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;

import java.time.LocalDateTime;

/**
 * CartItem 엔티티 테스트 픽스처
 * 테스트에서 자주 사용되는 CartItem 객체를 생성하는 유틸리티 클래스
 */
public class CartItemFixture {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 장바구니 아이템 생성
     */
    public static CartItem defaultCartItem() {
        return CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }
}