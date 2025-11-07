package io.hhplus.ecommerce.coupon.application.usecase;

import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CouponIssueUseCase {

    private final CouponService couponService;

    @Transactional
    public UserCoupon execute(Long userId, Long couponId) {
        return couponService.issueCoupon(userId, couponId);
    }

}
