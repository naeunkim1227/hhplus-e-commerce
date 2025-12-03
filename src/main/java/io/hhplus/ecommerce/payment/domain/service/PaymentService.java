package io.hhplus.ecommerce.payment.domain.service;

import io.hhplus.ecommerce.payment.domain.dto.command.PaymentProcessCommand;
import io.hhplus.ecommerce.payment.domain.event.PaymentFailureEvent;
import io.hhplus.ecommerce.payment.domain.event.PaymentSuccessEvent;
import io.hhplus.ecommerce.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 서비스 (타사 PG 연동 가정)
 * 실제로는 외부 API를 호출하지만, 여기서는 가상으로 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    public void processPayment(PaymentProcessCommand command) {
        log.info("결제 처리 시작 - orderId: {}, userId: {}, amount: {}",
                command.getOrderId(), command.getUserId(), command.getAmount());

        try {
            // 타사 PG 결제 처리 시뮬레이션
            Thread.sleep(2000);

            // 잔액 차감 (실패 시 예외 발생)
            userService.reduceBalance(command.getUserId(), command.getAmount());

            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);

            PaymentSuccessEvent event = new PaymentSuccessEvent(
                    command.getOrderId(),
                    command.getUserId(),
                    command.getAmount(),
                    paymentId,
                    LocalDateTime.now(),
                    command.getCartItemIds()
            );

            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // reduceBalance 실패 포함 모든 예외 처리
            PaymentFailureEvent event = new PaymentFailureEvent(
                    command.getOrderId(),
                    command.getUserId(),
                    command.getAmount(),
                    "결제 처리 실패: " + e.getMessage(),
                    LocalDateTime.now()
            );

            eventPublisher.publishEvent(event);
        }
    }
}