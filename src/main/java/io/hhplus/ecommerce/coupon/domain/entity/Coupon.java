package io.hhplus.ecommerce.coupon.domain.entity;

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
    private BigDecimal totalQuantity;
    private BigDecimal issuedQuantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private CouponStatus status;
    private CouponType type;
    private BigDecimal discountRate;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}