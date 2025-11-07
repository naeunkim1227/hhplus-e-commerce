package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.cart.domain.entity.CartItem;
import io.hhplus.ecommerce.cart.domain.service.CartService;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import io.hhplus.ecommerce.fixture.*;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCreateFromCartUseCase 단위 테스트")
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
    void createOrderFromCart_Success() {
        // Given: 장바구니 아이템 2개
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L, 2L))
                .couponId(null)
                .build();

        given(cartService.getCartItemsByIds(List.of(1L, 2L)))
                .willReturn(List.of(cartItem1, cartItem2));

        given(orderService.getNextOrderId()).willReturn(1L);

        given(productService.getProduct(1L)).willReturn(product1);
        given(productService.getProduct(2L)).willReturn(product2);


        OrderItem orderItem1 = OrderItemFixture.createOrderItem(1L, 1L, 2, product1.getPrice());
        OrderItem orderItem2 = OrderItemFixture.createOrderItem(1L, 2L, 1, product2.getPrice());
        given(orderService.createOrderItem(anyLong(), anyLong(), any(BigDecimal.class), anyInt()))
                .willReturn(orderItem1)
                .willReturn(orderItem2);

        given(orderService.createOrder(anyLong(), anyLong(), any(), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class)))
                .willReturn(order);

        doNothing().when(paymentService).processPayment(anyLong(), anyLong(),
                any(BigDecimal.class), anyList());

        // When: 주문 생성
        OrderDto result = orderCreateFromCartUseCase.excute(command);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(cartService, times(1)).getCartItemsByIds(List.of(1L, 2L));
        verify(productService, times(2)).getProduct(anyLong());
        verify(productService, times(2)).validate(any(Product.class), anyInt());
        verify(productService, times(2)).reserveStock(anyLong(), anyLong(), anyInt());
        verify(orderService, times(1)).createOrder(anyLong(), anyLong(), any(),
                any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class));
        verify(paymentService, times(1)).processPayment(anyLong(), anyLong(),
                any(BigDecimal.class), anyList());
    }

    @Test
    @DisplayName("장바구니에서 주문 생성 성공 (쿠폰 적용)")
    void createOrderFromCart_Coupon_Success() {
        // Given: 쿠폰 ID 포함
        OrderCreateFromCartCommand command = OrderCreateFromCartCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L, 2L))
                .couponId(1L)
                .build();

        // Mock 설정
        given(cartService.getCartItemsByIds(List.of(1L, 2L)))
                .willReturn(List.of(cartItem1, cartItem2));
        given(orderService.getNextOrderId()).willReturn(1L);
        given(productService.getProduct(1L)).willReturn(product1);
        given(productService.getProduct(2L)).willReturn(product2);
        doNothing().when(productService).validate(any(Product.class), anyInt());
        doNothing().when(productService).reserveStock(anyLong(), anyLong(), anyInt());

        OrderItem orderItem1 = OrderItemFixture.createOrderItem(1L, 1L, 2, product1.getPrice());
        OrderItem orderItem2 = OrderItemFixture.createOrderItem(1L, 2L, 1, product2.getPrice());
        given(orderService.createOrderItem(anyLong(), anyLong(), any(BigDecimal.class), anyInt()))
                .willReturn(orderItem1)
                .willReturn(orderItem2);

        // 쿠폰 검증 및 할인 금액 계산
        doNothing().when(couponService).validateCoupon(anyLong(), anyLong(), any(BigDecimal.class));
        given(couponService.calculateDisCountAmount(anyLong(), any(BigDecimal.class)))
                .willReturn(BigDecimal.valueOf(10000));

        Order orderWithCoupon = OrderFixture.orderWithCoupon();
        given(orderService.createOrder(anyLong(), anyLong(), anyLong(), any(BigDecimal.class),
                any(BigDecimal.class), any(BigDecimal.class)))
                .willReturn(orderWithCoupon);

        doNothing().when(paymentService).processPayment(anyLong(), anyLong(),
                any(BigDecimal.class), anyList());

        // When: 주문 생성
        OrderDto result = orderCreateFromCartUseCase.excute(command);

        // Then: 쿠폰 적용 검증
        assertThat(result).isNotNull();
        assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.valueOf(10000));

        // 쿠폰 서비스 호출 검증
        verify(couponService, times(1)).validateCoupon(anyLong(), anyLong(), any(BigDecimal.class));
        verify(couponService, times(1)).calculateDisCountAmount(anyLong(), any(BigDecimal.class));
    }
}