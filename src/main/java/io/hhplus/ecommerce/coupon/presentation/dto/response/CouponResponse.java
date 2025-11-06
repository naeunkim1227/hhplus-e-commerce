package io.hhplus.ecommerce.coupon.presentation.dto.response;

import io.hhplus.ecommerce.coupon.application.result.CouponDto;
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
public class CouponResponse {
    private Long id;
    private String code;
    private String name;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer remainingQuantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;

    public static CouponResponse fromResult(CouponDto result) {
        return CouponResponse.builder()
                .id(result.getId())
                .code(result.getCode())
                .name(result.getName())
                .totalQuantity(result.getTotalQuantity())
                .issuedQuantity(result.getIssuedQuantity())
                .remainingQuantity(result.getRemainingQuantity())
                .startDate(result.getStartDate())
                .endDate(result.getEndDate())
                .status(result.getStatus())
                .build();
    }
}