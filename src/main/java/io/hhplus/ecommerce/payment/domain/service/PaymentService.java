package io.hhplus.ecommerce.payment.domain.service;

import io.hhplus.ecommerce.payment.domain.event.PaymentFailureEvent;
import io.hhplus.ecommerce.payment.domain.event.PaymentSuccessEvent;
import io.hhplus.ecommerce.user.domain.service.UserService;
import io.hhplus.ecommerce.user.infrastructure.repositoty.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * 결제 처리 (비동기 가정)
     * Thread.sleep으로 외부 API 호출 시뮬레이션 처리
     * 결제 성공 시: 재고차감 + 주문완료 + 장바구니 비우기 + 재고 예약 확정 (이벤트 핸들러에서 처리)
     * 결제 실패 시: 재고 예약 해제 + 주문 실패 상태 변경 (이벤트 핸들러에서 처리)
     *
     * user의 balance를 유저 실제계좌의 잔액이라고 가정하고 타사 결제 서비스에서 해당 잔액을 차감한다고 가정한다.
     */
    public void processPayment(Long orderId, Long userId, BigDecimal amount, List<Long> cartItemIds) {
        log.info("결제 처리 시작 - ", orderId, userId, amount);

        try {
            // 타사 PG 결제 처리 시뮬레이션
            Thread.sleep(2000);

            // 잔액 차감 (실패 시 예외 발생)
            userService.reduceBalance(userId, amount);

            String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);

            PaymentSuccessEvent event = new PaymentSuccessEvent(
                    orderId,
                    userId,
                    amount,
                    paymentId,
                    LocalDateTime.now(),
                    cartItemIds
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // reduceBalance 실패 포함 모든 예외 처리
            log.error("결제 처리 중 오류 발생 - orderId: {}", orderId, e);
            PaymentFailureEvent event = new PaymentFailureEvent(
                    orderId,
                    userId,
                    amount,
                    "결제 처리 실패: " + e.getMessage(),
                    LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * 결제 취소 (환불)
     */
    public void cancelPayment(Long orderId, String paymentId, BigDecimal amount) {
        log.info("결제 취소 시작 - orderId: {}, paymentId: {}, amount: {}", orderId, paymentId, amount);

        try {
            // 타사 PG 환불 처리 시뮬레이션
            Thread.sleep(1000);
            log.info("결제 취소 완료 - orderId: {}", orderId);

        } catch (InterruptedException e) {
            log.error("결제 취소 중 인터럽트 발생 - orderId: {}", orderId, e);
            Thread.currentThread().interrupt();
        }
    }
}