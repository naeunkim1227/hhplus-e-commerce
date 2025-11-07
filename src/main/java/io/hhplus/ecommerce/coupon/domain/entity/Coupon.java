package io.hhplus.ecommerce.coupon.domain.entity;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private Long id;
    private String code;
    private String name;
    private int totalQuantity;
    private int issuedQuantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CouponStatus status;
    private CouponType type;
    private BigDecimal discountRate;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 쿠폰 발급 가능 여부 검증
     */
    public void isAvailableIssue() {
        if(this.issuedQuantity >= this.totalQuantity){
            throw new BusinessException(CouponErrorCode.COUPON_SOLD_OUT);
        }

        if(status != CouponStatus.ACTIVE || LocalDateTime.now().isAfter(endDate)) {
            throw new BusinessException(CouponErrorCode.COUPON_EXPIRED);
        }
    }

    /**
     * 쿠폰 발급 수량 증가
     */
    public void increaseIssuedQuantity() {
        this.issuedQuantity++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 생성
     */
    public static Coupon create(String code, String name, CouponType type, BigDecimal discountRate) {
        return Coupon.builder()
                .code(code)
                .name(name)
                .type(type)
                .discountRate(discountRate)
                .build();
    }
}