package io.hhplus.ecommerce.order.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 커맨드 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDirectCommand {
    private Long userId;
    private Long couponId;
    private Long productId;
    private int quantity;
}