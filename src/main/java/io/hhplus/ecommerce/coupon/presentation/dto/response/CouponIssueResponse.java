package io.hhplus.ecommerce.coupon.presentation.dto.response;

import io.hhplus.ecommerce.coupon.application.result.CouponDto;
import io.hhplus.ecommerce.coupon.application.result.CouponIssueDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 쿠폰 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueResponse {
    private Long id;
    private Long waitNumber;


    public static CouponIssueResponse fromResult(CouponIssueDto result) {
        return CouponIssueResponse.builder()
                .id(result.getId())
                .waitNumber(result.getWaitNumber())
                .build();
    }
}