package io.hhplus.ecommerce.cart.domain.service;

import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.repository.CartRepository;
import io.hhplus.ecommerce.cart.domain.validator.CartValidator;
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

    @Mock
    private CartValidator cartValidator;

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
    void addCartItem_Success() {
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
    @DisplayName("장바구니에 상품 수량을 수정할 수 있다")
    void updateCartItem_Success() {
        //Given
        //이미 담겨져 있는 상품
        CartItem existingCartItem = CartItemFixture.defaultCartItem();
        given(cartRepository.findById(1L))
                .willReturn(Optional.of(existingCartItem));

        //저장 후 상품
        CartItem savedCartItem = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(5)
                .build();
        given(cartRepository.save(any(CartItem.class)))
                .willReturn(savedCartItem);


        CartItemUpdateCommand updateCommand = CartItemUpdateCommand.builder()
                .userId(1L)
                .cartItemId(1L)
                .quantity(4)
                .build();

        // When
        CartItem result = cartService.updateCartItem(updateCommand);

        // Then: 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getQuantity()).isEqualTo(5);

        verify(cartRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }


    @Test
    @DisplayName("cartItemId로 다건을 조회할 수 있다")
    void getCartItemsByIds_Success() {
        // Given
        CartItem cartItem1 = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .id(2L)
                .userId(1L)
                .productId(2L)
                .quantity(5)
                .build();

        List<Long> cartItemIds = List.of(1L, 2L);

        given(cartRepository.findByIdIn(cartItemIds))
                .willReturn(List.of(cartItem1, cartItem2));

        // When
        List<CartItem> cartItems = cartService.getCartItemsByIds(cartItemIds);

        // Then
        assertThat(cartItems).isNotNull();
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems.get(0).getId()).isEqualTo(1L);
        assertThat(cartItems.get(0).getQuantity()).isEqualTo(1);
        assertThat(cartItems.get(1).getId()).isEqualTo(2L);
        assertThat(cartItems.get(1).getQuantity()).isEqualTo(5);

        verify(cartRepository, times(1)).findByIdIn(cartItemIds);
    }



    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    void clearCart_Success() {
        // Given
        CartItem cartItem1 = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .id(2L)
                .userId(1L)
                .productId(2L)
                .quantity(5)
                .build();

        List<CartItem> cartItems = List.of(cartItem1, cartItem2);
        given(cartRepository.findByUserId(1L))
                .willReturn(cartItems);

        // When
        cartService.clearCart(1L);
        // Then
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartRepository, times(1)).deleteByUserId(1L);
    }


    @Test
    @DisplayName("장바구니 선택 삭제 성공")
    void deleteCart_Success() {
        // Given
        Long cartItemId = 1L;
        CartItem existingCartItem = CartItemFixture.defaultCartItem();
        CartItemDeleteCommand command = CartItemDeleteCommand.builder()
                .userId(1L)
                .cartItemId(cartItemId)
                .build();

        given(cartRepository.findById(cartItemId))
                .willReturn(Optional.of(existingCartItem));

        // When
        cartService.deleteCartItem(command);
        // Then
        verify(cartRepository, times(1)).findById(cartItemId);
        verify(cartRepository, times(1)).deleteById(cartItemId);
        verify(cartValidator, times(1)).validate(1L, existingCartItem);
    }

}
