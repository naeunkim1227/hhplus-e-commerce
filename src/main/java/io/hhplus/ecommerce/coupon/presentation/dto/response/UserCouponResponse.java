package io.hhplus.ecommerce.coupon.presentation.dto.response;

import io.hhplus.ecommerce.coupon.application.result.UserCouponDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 쿠폰 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponResponse {
    private Long id;
    private Long userId;
    private Long couponId;
    private String couponCode;
    private String couponName;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;

    public static UserCouponResponse from(UserCouponDto result) {
        return UserCouponResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .couponId(result.getCouponId())
                .couponCode(result.getCouponCode())
                .couponName(result.getCouponName())
                .issuedAt(result.getIssuedAt())
                .usedAt(result.getUsedAt())
                .expiredAt(result.getExpiredAt())
                .build();
    }
}