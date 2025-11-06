package io.hhplus.ecommerce.coupon.domain.validator;

import io.hhplus.ecommerce.common.exception.BusinessException;
import io.hhplus.ecommerce.coupon.domain.CouponPolicy;
import io.hhplus.ecommerce.coupon.domain.entity.Coupon;
import io.hhplus.ecommerce.coupon.domain.entity.CouponStatus;
import io.hhplus.ecommerce.coupon.domain.entity.UserCoupon;
import io.hhplus.ecommerce.coupon.domain.exception.CouponErrorCode;
import io.hhplus.ecommerce.coupon.domain.repository.CouponRepository;
import io.hhplus.ecommerce.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CouponValidator {
    private final CouponPolicy couponPolicy;

    public void validate(Coupon coupon, UserCoupon userCoupon, BigDecimal amount) {
        validateStatus(coupon);
        validatePolicy(coupon, amount);
        validateCouponToUser(userCoupon);
    }

    /**
     * 쿠폰 가능 상태 검증
     */
    public void validateStatus(Coupon coupon) {
        if(!coupon.getStatus().equals(CouponStatus.ACTIVE)){
            throw new BusinessException(CouponErrorCode.COUPON_NOT_AVAILABLE);
        }
    }

    /**
     * 정책 검증
     */
    public void validatePolicy(Coupon coupon,BigDecimal amount) {
      int minAmount = couponPolicy.getMinOrderAmount(coupon.getType()).compareTo(amount);
        if(minAmount > 0){
            throw new BusinessException(CouponErrorCode.COUPON_MINIMUM_AMOUNT_NOT_MET);
        }

        int maxAmount = couponPolicy.getMaxOrderAmount(coupon.getType()).compareTo(amount);
        if(maxAmount < 0){
            throw new BusinessException(CouponErrorCode.COUPON_MAXIMUM_AMOUNT_NOT_MET);
        }
    }


    /**
     * 사용자가 이미 사용하였는지, 만료일이 지나지 않았는지 확인
     */
    public void validateCouponToUser(UserCoupon userCoupon) {
        if(userCoupon.isExpired()){
            throw new BusinessException(CouponErrorCode.COUPON_EXPIRED);
        }

        if(userCoupon.isUsed()){
            throw new BusinessException(CouponErrorCode.COUPON_ALREADY_USED);
        }
    }

}
