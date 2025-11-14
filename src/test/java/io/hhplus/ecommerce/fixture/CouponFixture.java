package io.hhplus.ecommerce.fixture;

import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon 엔티티 테스트 픽스처
 */
public class CouponFixture {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 쿠폰 생성 (10% 할인)
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static Coupon defaultCoupon() {
        return Coupon.builder()
                .code("COUPON10")
                .name("10% 할인 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(5)
                .startDate(DEFAULT_TIMESTAMP)
                .endDate(DEFAULT_TIMESTAMP.plusDays(30))
                .status(CouponStatus.ACTIVE)
                .type(CouponType.RATE)
                .discountRate(BigDecimal.valueOf(10))
                .version(0L)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }



    /**
     * 품절된 쿠폰
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static Coupon soldOutCoupon() {
        return Coupon.builder()
                .code("SOLDOUT")
                .name("품절 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .startDate(DEFAULT_TIMESTAMP)
                .endDate(DEFAULT_TIMESTAMP.plusDays(30))
                .status(CouponStatus.ACTIVE)
                .type(CouponType.RATE)
                .discountRate(BigDecimal.valueOf(20))
                .version(0L)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 만료된 쿠폰
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static Coupon expiredCoupon() {
        return Coupon.builder()
                .code("EXPIRED")
                .name("만료 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(10)
                .startDate(DEFAULT_TIMESTAMP.minusDays(60))
                .endDate(DEFAULT_TIMESTAMP.minusDays(30))
                .status(CouponStatus.INACTIVE)
                .type(CouponType.RATE)
                .discountRate(BigDecimal.valueOf(15))
                .version(0L)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }
}