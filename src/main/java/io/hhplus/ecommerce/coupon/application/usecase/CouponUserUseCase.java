package io.hhplus.ecommerce.coupon.application.usecase;

import io.hhplus.ecommerce.coupon.application.result.CouponDto;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.service.CouponService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CouponUserUseCase {

    private final CouponService couponService;

    public List<UserCoupon> excute(Long userId) {
        return couponService.getUserCoupon(userId);
    }

}
