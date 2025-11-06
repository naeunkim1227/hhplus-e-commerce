package io.hhplus.ecommerce.coupon.domain.entity;

public enum CouponStatus {
    ACTIVE,      // 발급 가능
    INACTIVE,    // 발급 중지
    EXPIRED,     // 기간 만료
    SOLD_OUT,     // 소진됨
}