package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import io.hhplus.ecommerce.coupon.domain.validator.CouponValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponValidator couponValidator;

    /**
     * 쿠폰 조회
     */
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
    }

    /**
     * 유저의 쿠폰 조회
     */
    public List<UserCoupon> getUserCoupon(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }

    /**
     * 쿠폰 유효성 검증
     */
    public void validateCoupon(Long couponId, Long userId, BigDecimal amount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
        couponValidator.validate(coupon, userCoupon, amount);
    }

    /**
     * 쿠폰 할인액 계산
     */
    public BigDecimal calculateDisCountAmount(Long couponId, BigDecimal totalAmount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
        BigDecimal discountAmount = BigDecimal.ZERO;

        switch (coupon.getType()) {
            case RATE ->  discountAmount = totalAmount.multiply(coupon.getDiscountRate());
            case FIXED -> discountAmount = totalAmount.subtract(coupon.getDiscountRate());
        }
        return discountAmount;
    }
}
