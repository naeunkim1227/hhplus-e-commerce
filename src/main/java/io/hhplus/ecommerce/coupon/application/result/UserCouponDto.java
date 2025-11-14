package io.hhplus.ecommerce.coupon.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 결과 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponDto {
    private Long id;
    private Long userId;
    private Long couponId;
    private String couponCode;
    private String couponName;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;
}