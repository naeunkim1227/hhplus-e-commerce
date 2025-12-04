package io.hhplus.ecommerce.coupon.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 쿠폰 결과 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueDto {
    private Long id;
    private Long userId;
    private Long waitNumber;

    public static CouponIssueDto from(Long id, Long userId, Long waitNumber) {
        return CouponIssueDto.builder()
                .id(id)
                .userId(userId)
                .waitNumber(waitNumber)
                .build();
    }

}






