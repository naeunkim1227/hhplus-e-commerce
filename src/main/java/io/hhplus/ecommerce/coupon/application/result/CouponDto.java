package io.hhplus.ecommerce.coupon.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 쿠폰 조회 결과 (Application Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    private Long id;
    private String code;
    private String name;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer remainingQuantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}