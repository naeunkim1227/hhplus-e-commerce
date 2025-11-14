package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.fixture.*;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.dto.OrderInfo;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.exception.OrderErrorCode;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import io.hhplus.ecommerce.payment.domain.service.PaymentService;
import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCreateFromCartUseCase 단위 테스트 - 리팩토링 후")
class OrderCreateFromCartUseCaseTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private CouponService couponService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderCreateFromCartUseCase orderCreateFromCartUseCase;

    private Product product1;
    private Product product2;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private Order order;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        product1 = ProductFixture.defaultProduct();
        product2 = Product.builder()
                .id(2L)
                .name("아메리카노")
                .price(BigDecimal.valueOf(4500))
                .stock(100L)
                .build();

        cartItem1 = CartItem.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .build();

        cartItem2 = CartItem.builder()
                .id(2L)
                .userId(1L)
                .productId(2L)
                .quantity(1)
                .build();

        order = OrderFixture.defaultOrder();
    }

    @Test
    @DisplayName("장바구니에서 주문 생성 성공 (쿠폰 없음)")
    void createOrderFromCart_Success_WithoutCoupon() {
        // Given: 쿠폰 없는 장바구니 주문
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L, 2L))
                .couponId(null)
                .build();

        // 1. 장바구니 아이템 조회
        given(cartService.getCartItemsByIds(List.of(1L, 2L)))
                .willReturn(List.of(cartItem1, cartItem2));

        // 2. orderId 생성
        given(orderService.getNextOrderId()).willReturn(1L);

        // 3. 재고 선점
        given(productService.reserveStock(1L, 1L, 2))
                .willReturn(product1);
        given(productService.reserveStock(1L, 2L, 1))
                .willReturn(product2);

        // 5. 주문 생성
        given(orderService.createOrderFromCart(any(OrderInfo.class)))
                .willReturn(order);

        // 6. 결제 처리
        doNothing().when(paymentService).processPayment(
                anyLong(), anyLong(), any(BigDecimal.class), anyList());

        // When: 주문 생성 실행
        OrderDto result = orderCreateFromCartUseCase.excute(command);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);

        // 서비스 호출 검증
        verify(cartService, times(1)).getCartItemsByIds(List.of(1L, 2L));
        verify(orderService, times(1)).getNextOrderId();
        verify(productService, times(2)).reserveStock(anyLong(), anyLong(), anyInt());
        verify(couponService, never()).validateCoupon(anyLong(), anyLong(), any(BigDecimal.class));
        verify(orderService, times(1)).createOrderFromCart(any(OrderInfo.class));
        verify(paymentService, times(1)).processPayment(
                eq(1L), eq(1L), any(BigDecimal.class), eq(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("장바구니에서 주문 생성 성공 (쿠폰 적용)")
    void createOrderFromCart_Success_WithCoupon() {
        // Given: 쿠폰 포함 장바구니 주문
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L, 2L))
                .couponId(10L)
                .build();

        // 1. 장바구니 아이템 조회
        given(cartService.getCartItemsByIds(List.of(1L, 2L)))
                .willReturn(List.of(cartItem1, cartItem2));

        // 2. orderId 생성
        given(orderService.getNextOrderId()).willReturn(1L);

        // 3. 재고 선점
        given(productService.reserveStock(1L, 1L, 2))
                .willReturn(product1);
        given(productService.reserveStock(1L, 2L, 1))
                .willReturn(product2);

        // 4. 주문 생성 (쿠폰 할인 적용)
        Order orderWithCoupon = OrderFixture.orderWithCoupon();
        given(orderService.createOrderFromCart(any(OrderInfo.class)))
                .willReturn(orderWithCoupon);

        // 6. 결제 처리
        doNothing().when(paymentService).processPayment(
                anyLong(), anyLong(), any(BigDecimal.class), anyList());

        // When: 주문 생성 실행
        OrderDto result = orderCreateFromCartUseCase.excute(command);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));


        // 서비스 호출 검증
        verify(cartService, times(1)).getCartItemsByIds(List.of(1L, 2L));
        verify(orderService, times(1)).getNextOrderId();
        verify(productService, times(2)).reserveStock(anyLong(), anyLong(), anyInt());
        verify(orderService, times(1)).createOrderFromCart(any(OrderInfo.class));
        verify(paymentService, times(1)).processPayment(
                eq(1L), eq(1L), any(BigDecimal.class), eq(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("장바구니 아이템이 없으면 실패")
    void createOrderFromCart_Fail_EmptyCart() {
        // Given: 빈 장바구니
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(1L)
                .cartItemIds(List.of())
                .couponId(null)
                .build();

        given(cartService.getCartItemsByIds(List.of()))
                .willReturn(List.of());

        assertThatThrownBy(() -> orderCreateFromCartUseCase.excute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage(OrderErrorCode.CART_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("재고 선점 실패 시 예외 발생")
    void createOrderFromCart_Fail_OutOfStock() {
        // Given: 재고 부족
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L))
                .couponId(null)
                .build();

        given(cartService.getCartItemsByIds(List.of(1L)))
                .willReturn(List.of(cartItem1));

        given(orderService.getNextOrderId()).willReturn(1L);

        // 재고 선점 실패 (ProductService 내부에서 검증 후 예외 발생)
        given(productService.reserveStock(1L, 1L, 2))
                .willThrow(new IllegalStateException("재고 부족"));

        // When & Then: 예외 발생
        assertThatThrownBy(() -> orderCreateFromCartUseCase.excute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고 부족");

        // 재고 선점 실패 시 주문 생성 및 결제는 호출되지 않음
        verify(orderService, never()).createOrderFromCart(any(OrderInfo.class));
        verify(paymentService, never()).processPayment(
                anyLong(), anyLong(), any(BigDecimal.class), anyList());
    }


}