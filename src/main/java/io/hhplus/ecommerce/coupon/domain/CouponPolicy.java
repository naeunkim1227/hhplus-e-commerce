package io.hhplus.ecommerce.coupon.domain;

import io.hhplus.ecommerce.coupon.domain.entity.CouponType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CouponPolicy {

    private static final BigDecimal RATE_MIN_ORDER_AMOUNT = BigDecimal.valueOf(50000);
    private static final BigDecimal FIXED_MIN_ORDER_AMOUNT = BigDecimal.valueOf(30000);

    public BigDecimal getMinOrderAmount(CouponType type) {
        return switch (type) {
            case RATE  -> RATE_MIN_ORDER_AMOUNT;
            case FIXED -> FIXED_MIN_ORDER_AMOUNT;
        };
    }

    public BigDecimal getMaxOrderAmount(CouponType type) {
        return switch (type) {
            case RATE  -> BigDecimal.valueOf(1000000);
            case FIXED -> BigDecimal.valueOf(9999999);
        };
    }
}