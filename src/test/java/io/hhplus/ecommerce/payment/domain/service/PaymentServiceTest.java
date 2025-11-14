package io.hhplus.ecommerce.payment.domain.service;

import io.hhplus.ecommerce.payment.domain.event.PaymentFailureEvent;
import io.hhplus.ecommerce.payment.domain.event.PaymentSuccessEvent;
import io.hhplus.ecommerce.user.domain.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 결제 이벤트 테스트")
class PaymentServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 성공 시 이벤트 발행 테스트")
    void PublishesSuccessEvent() {
        // Given: 결제 정보
        Long orderId = 1L;
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100000);
        List<Long> cartItemIds = List.of(1L, 2L);

        doNothing().when(userService).reduceBalance(userId, amount);

        // ArgumentCaptor로 발행되는 이벤트 캡처
        ArgumentCaptor<PaymentSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);

        // When: 결제 처리
        paymentService.processPayment(orderId, userId, amount, cartItemIds);

        // Then: PaymentSuccessEvent가 발행되었는지 검증
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        // 발행된 이벤트 내용 검증
        PaymentSuccessEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getUserId()).isEqualTo(userId);
        assertThat(capturedEvent.getAmount()).isEqualTo(amount);
        assertThat(capturedEvent.getCartItemIds()).containsExactly(1L, 2L);
        assertThat(capturedEvent.getPaymentId()).startsWith("PAY-");

        // UserService의 잔액 차감 호출 검증
        verify(userService, times(1)).reduceBalance(userId, amount);
    }

    @Test
    @DisplayName("결제 실패 시 PaymentFailureEvent가 발행된다")
    void PublishesFailureEvent() {
        // Given: 결제 정보
        Long orderId = 1L;
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100000);
        List<Long> cartItemIds = List.of(1L, 2L);

        // Mock 설정: 잔액 차감 실패 (예외 발생)
        doThrow(new RuntimeException("잔액 부족"))
                .when(userService).reduceBalance(userId, amount);

        // ArgumentCaptor로 발행되는 이벤트 캡처
        ArgumentCaptor<PaymentFailureEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailureEvent.class);

        // When: 결제 처리
        paymentService.processPayment(orderId, userId, amount, cartItemIds);

        // Then: PaymentFailureEvent가 발행되었는지 검증
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        // 발행된 이벤트 내용 검증
        PaymentFailureEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getUserId()).isEqualTo(userId);
        assertThat(capturedEvent.getAmount()).isEqualTo(amount);
        assertThat(capturedEvent.getFailureReason()).contains("잔액 부족");

        // UserService의 잔액 차감 호출 검증
        verify(userService, times(1)).reduceBalance(userId, amount);
    }
}