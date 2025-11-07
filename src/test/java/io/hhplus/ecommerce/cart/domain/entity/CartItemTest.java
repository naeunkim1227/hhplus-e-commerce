package io.hhplus.ecommerce.cart.domain.entity;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CartItem 엔티티 단위 테스트")
class CartItemTest {

    @Test
    @DisplayName("유효한 값으로 장바구니 아이템을 생성하면 성공한다")
    void create_WithValidData_Success() {
        // Given
        CartItemAddCommand command = CartItemAddCommand.builder()
                .userId(1L)
                .productId(100L)
                .quantity(3)
                .build();

        // When
        CartItem cartItem = CartItem.create(command);

        // Then
        assertThat(cartItem).isNotNull();
        assertThat(cartItem.getUserId()).isEqualTo(1L);
        assertThat(cartItem.getProductId()).isEqualTo(100L);
        assertThat(cartItem.getQuantity()).isEqualTo(3);
        assertThat(cartItem.getCreatedAt()).isNotNull();
        assertThat(cartItem.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("장바구니 아이템을 빌더로 생성한다")
    void builder_Success() {
        // Given & When
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .userId(10L)
                .productId(200L)
                .quantity(5)
                .build();

        // Then
        assertThat(cartItem).isNotNull();
        assertThat(cartItem.getId()).isEqualTo(1L);
        assertThat(cartItem.getUserId()).isEqualTo(10L);
        assertThat(cartItem.getProductId()).isEqualTo(200L);
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("동일한 사용자와 상품의 장바구니 아이템은 구분된다")
    void create_MultipleItemsSameUserAndProduct_AreDifferent() {
        // Given
        CartItemAddCommand command1 = CartItemAddCommand.builder()
                .userId(1L)
                .productId(100L)
                .quantity(2)
                .build();

        CartItemAddCommand command2 = CartItemAddCommand.builder()
                .userId(1L)
                .productId(100L)
                .quantity(3)
                .build();

        // When
        CartItem cartItem1 = CartItem.create(command1);
        CartItem cartItem2 = CartItem.create(command2);

        // Then
        assertThat(cartItem1).isNotEqualTo(cartItem2);
        assertThat(cartItem1.getQuantity()).isEqualTo(2);
        assertThat(cartItem2.getQuantity()).isEqualTo(3);
    }
}