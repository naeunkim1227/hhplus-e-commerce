package io.hhplus.ecommerce.order.presentation.dto.request;

import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 주문 생성 요청 DTO (Presentation Layer)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateFromCartRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    private Long couponId;

    @NotEmpty(message = "주문할 장바구니 아이템을 선택하세요")
    private List<Long> cartItemIds;

    public OrderCreateFromCartCommand toCommand() {
        return OrderCreateFromCartCommand.builder()
                .userId(userId)
                .couponId(couponId)
                .cartItemIds(cartItemIds)
                .build();
    }
}