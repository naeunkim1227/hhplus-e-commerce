package io.hhplus.ecommerce.order.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.entity.OrderItem;
import io.hhplus.ecommerce.order.domain.entity.OrderStatus;
import io.hhplus.ecommerce.order.domain.exception.OrderErrorCode;
import io.hhplus.ecommerce.order.domain.repository.OrderItemRepository;
import io.hhplus.ecommerce.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName( "OrderService 단위 Test")
public class OrderServiceTest  {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderService orderService;

    Order fixedOrder;


    @BeforeEach
    void setUp() {
        fixedOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(BigDecimal.valueOf(10000))
                .build();
    }

    @DisplayName("주문 조회 성공")
    @Test
    void getOrders_Success(){
        given(orderRepository.findByIdWithOrderItems(1L)).willReturn(Optional.of(fixedOrder));

        Order orderResult = orderService.getOrder(1L);

        assertThat(orderResult).isNotNull();
        assertThat(orderResult.getId()).isEqualTo(1L);
        assertThat(orderResult.getUserId()).isEqualTo(1L);
        assertThat(orderResult.getTotalAmount()).isEqualTo(BigDecimal.valueOf(10000));

        verify(orderRepository, times(1)).findByIdWithOrderItems(1L);
    }

    @DisplayName("유효하지 않은 주문아이디로 조회시 주문을 찾을 수 없다")
    @Test
    void getOrders_WithoutId_Fail(){
        given(orderRepository.findByIdWithOrderItems(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(2L))
                .isInstanceOf(BusinessException.class)
                        .hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());

        verify(orderRepository, times(1)).findByIdWithOrderItems(2L);
    }


    @DisplayName("주문 완료 상태로 상태를 변경할 수 있다.")
    @Test
    void completeOrder_Success(){
        // Given
        Order pendingOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(10000))
                .build();

        given(orderRepository.findByIdWithOrderItems(1L)).willReturn(Optional.of(pendingOrder));

        // When
        orderService.completeOrder(1L);

        // Then
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(pendingOrder.getUpdatedAt()).isNotNull();
        verify(orderRepository, times(1)).findByIdWithOrderItems(1L);
        verify(orderRepository, times(1)).save(pendingOrder);
    }


    @DisplayName("실패 상태로 상태를 변경할 수 있다.")
    @Test
    void failOrder_Success(){
        // Given
        Order pendingOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(10000))
                .build();

        given(orderRepository.findByIdWithOrderItems(1L)).willReturn(Optional.of(pendingOrder));

        // When
        orderService.failOrder(1L);

        // Then
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(pendingOrder.getUpdatedAt()).isNotNull();
        verify(orderRepository, times(1)).findByIdWithOrderItems(1L);
        verify(orderRepository, times(1)).save(pendingOrder);
    }

    @DisplayName("주문 상세 정보를 조회할 수 있다")
    @Test
    void getOrderItems_Success(){
        //given
        OrderItem orderItem1 = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(2)
                .price(BigDecimal.valueOf(10000))
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .orderId(1L)
                .productId(2L)
                .quantity(3)
                .price(BigDecimal.valueOf(10000))
                .build();

       List<OrderItem> orderItems =  List.of(orderItem1, orderItem2);
       given(orderItemRepository.findByOrderId(1L)).willReturn(orderItems);

       //when
       List<OrderItem> result = orderService.getOrderItems(1L);

       //then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
        assertThat(result.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @DisplayName("주문을 생성할 수 있다")
    @Test
    void createOrder_Success(){
        // Given
        BigDecimal totalAmount = BigDecimal.valueOf(50000);
        BigDecimal discountAmount = BigDecimal.valueOf(5000);
        BigDecimal finalAmount = BigDecimal.valueOf(45000);

        Order savedOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .couponId(100L)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .orderedAt(LocalDateTime.now())
                .build();

        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // When
        Order result = orderService.createOrder(1L, 100L, 1L, totalAmount, discountAmount, finalAmount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCouponId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(result.getDiscountAmount()).isEqualTo(discountAmount);
        assertThat(result.getFinalAmount()).isEqualTo(finalAmount);
        assertThat(result.getOrderedAt()).isNotNull();

        verify(orderRepository, times(1)).save(any(Order.class));
    }




}
