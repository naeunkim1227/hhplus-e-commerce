package io.hhplus.ecommerce.coupon.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠폰 발급 커맨드 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueCommand {
    private Long couponId;
    private Long userId;
}