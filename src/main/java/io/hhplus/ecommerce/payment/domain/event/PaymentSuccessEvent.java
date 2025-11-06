package io.hhplus.ecommerce.payment.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 성공 이벤트
 */
@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentId;
    private LocalDateTime paidAt;
    private List<Long> cartItemIds;  // 장바구니 비우기용
}