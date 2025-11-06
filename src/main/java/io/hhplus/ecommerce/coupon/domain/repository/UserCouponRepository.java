package io.hhplus.ecommerce.coupon.domain.repository;

import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {
    UserCoupon save(UserCoupon userCoupon);
    Optional<UserCoupon> findById(Long id);
    List<UserCoupon> findByUserId(Long userId);
    Optional<UserCoupon>  findByUserIdAndCouponId(Long userId, Long couponId);
    List<UserCoupon> findByCouponId(Long couponId);

}