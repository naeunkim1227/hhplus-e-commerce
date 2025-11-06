package io.hhplus.ecommerce.cart.domain.service;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import io.hhplus.ecommerce.fixture.CartItemFixture;
import io.hhplus.ecommerce.fixture.ProductFixture;
import io.hhplus.ecommerce.fixture.UserFixture;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 단위 테스트")
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.defaultUser();
        product = ProductFixture.defaultProduct();
    }

    @Test
    @DisplayName("장바구니를 조회할 수 있다.")
    void getCart_Success() {
        // Given
        List<CartItem> mockCartItems = List.of(CartItemFixture.defaultCartItem());
        given(cartRepository.findByUserId(1L))
                .willReturn(mockCartItems);

        // When: 조회
        List<CartItem> cartItems = cartService.getCartItems(1L);

        // Then
        assertThat(cartItems).isNotNull();
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.get(0).getUserId()).isEqualTo(1L);
        assertThat(cartItems.get(0).getQuantity()).isEqualTo(1);

        // Repository 호출 검증
        verify(cartRepository, times(1)).findByUserId(1L);
    }


    @Test
    @DisplayName("장바구니에 상품을 추가할 수 있다")
    void AddCartItem_Success() {
        CartItemAddCommand command = CartItemAddCommand.builder()
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .build();

        given(cartRepository.findByUserIdAndProductId(1L, 1L))
                .willReturn(Optional.empty());

        CartItem savedCartItem = CartItemFixture.defaultCartItem();
        given(cartRepository.save(any(CartItem.class)))
                .willReturn(savedCartItem);

        CartItem result = cartService.addOrUpdateCartItem(command);

        // Then: 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(1L);
        verify(cartRepository, times(1)).findByUserIdAndProductId(1L, 1L);
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    void clearCart_Success() {
        // Given
        Long userId = 1L;

        // When
        cartService.clearCart(userId);

        // Then
        verify(cartRepository, times(1)).deleteByUserId(userId);
    }


    @Test
    @DisplayName("장바구니 선택 삭제 성공")
    void deleteCart_Success() {
        // Given
        Long cartItemId = 1L;
        CartItemDeleteCommand command = CartItemDeleteCommand.builder()
                .userId(1L)
                .cartItemId(cartItemId)
                .build();
        // Whe
        cartService.deleteCartItem(command);
        // Then
        verify(cartRepository, times(1)).deleteById(cartItemId);
    }

}
