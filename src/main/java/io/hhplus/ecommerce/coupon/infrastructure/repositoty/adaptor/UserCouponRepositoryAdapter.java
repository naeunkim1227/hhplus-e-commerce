package io.hhplus.ecommerce.coupon.infrastructure.repositoty.adaptor;

import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import io.hhplus.ecommerce.coupon.infrastructure.repositoty.jpa.JpaUserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class UserCouponRepositoryAdapter implements UserCouponRepository {

    private final JpaUserCouponRepository jpaRepository;

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return jpaRepository.save(userCoupon);
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {
        return jpaRepository.findByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public List<UserCoupon> findByCouponId(Long couponId) {
        return jpaRepository.findByCouponId(couponId);
    }
}