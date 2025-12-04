package io.hhplus.ecommerce.coupon.domain.service;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.common.lock.DistributedLock;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import io.hhplus.ecommerce.coupon.domain.validator.CouponValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

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
    @Transactional(readOnly = true)
    public void validateCoupon(Long couponId, Long userId, BigDecimal amount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.USER_COUPON_NOT_FOUND));
        couponValidator.validate(coupon, userCoupon, amount);
    }

    /**
     * 쿠폰 할인액 계산
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDisCountAmount(Long couponId, BigDecimal totalAmount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));
        BigDecimal discountAmount = BigDecimal.ZERO;

        switch (coupon.getType()) {
            case RATE ->  discountAmount = totalAmount.multiply(coupon.getDiscountRate())
                    .divide(BigDecimal.valueOf(100), RoundingMode.DOWN);
            case FIXED -> discountAmount = totalAmount.subtract(coupon.getDiscountRate());
        }
        return discountAmount;
    }

    /**
     * 쿠폰 발급 (Stream Consumer에서 호출)
     * Redis에서 이미 중복/재고 체크가 완료되었으므로, DB 저장만 수행
     */
    @Transactional
    public UserCoupon issueCoupon(Long userId, Long couponId) {
        Optional<UserCoupon> existing = userCouponRepository.findByUserIdAndCouponId(userId, couponId);
        if (existing.isPresent()) {
            return existing.get();  // 중복이면 기존 쿠폰 반환 (멱등성 보장)
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(CouponErrorCode.COUPON_NOT_FOUND));

        coupon.increaseIssuedQuantity();

        UserCoupon userCoupon = UserCoupon.create(userId, couponId);
        return userCouponRepository.save(userCoupon);
    }
}
