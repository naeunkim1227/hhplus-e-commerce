package io.hhplus.ecommerce.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderCompletedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private List<Long> cartItemIds;
}
