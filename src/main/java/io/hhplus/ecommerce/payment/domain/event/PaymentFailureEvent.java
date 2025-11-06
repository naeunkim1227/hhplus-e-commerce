package io.hhplus.ecommerce.payment.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 실패 이벤트
 */
@Getter
@AllArgsConstructor
public class PaymentFailureEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String failureReason;
    private LocalDateTime failedAt;
}