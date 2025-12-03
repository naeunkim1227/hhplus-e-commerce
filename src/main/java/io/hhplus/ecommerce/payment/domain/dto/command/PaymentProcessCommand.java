package io.hhplus.ecommerce.payment.domain.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 결제 처리 요청 커맨드
 */
@Getter
@AllArgsConstructor
public class PaymentProcessCommand {
    private final Long orderId;
    private final Long userId;
    private final BigDecimal amount;
    private final List<Long> cartItemIds;

    public static PaymentProcessCommand of(Long orderId, Long userId, BigDecimal amount, List<Long> cartItemIds) {
        return new PaymentProcessCommand(orderId, userId, amount, cartItemIds);
    }

}