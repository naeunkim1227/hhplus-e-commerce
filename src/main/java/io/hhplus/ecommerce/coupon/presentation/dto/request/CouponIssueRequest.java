package io.hhplus.ecommerce.coupon.presentation.dto.request;

import io.hhplus.ecommerce.coupon.application.dto.command.CouponIssueCommand;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 쿠폰 발급 요청 DTO (Presentation Layer)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueRequest {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    private Long couponId;

    public CouponIssueCommand toCommand() {
        return CouponIssueCommand.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
    }
}