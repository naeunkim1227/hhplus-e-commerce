package io.hhplus.ecommerce.coupon.domain.exception;

import io.hhplus.ecommerce.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {
    //400
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST,"COUPON_NOT_FOUND","쿠폰을 찾을 수 없습니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST,"COUPON_EXPIRED","만료된 쿠폰입니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST,"COUPON_ALREADY_USED","이미 사용된 쿠폰입니다"),
    COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST,"COUPON_NOT_AVAILABLE","사용할 수 없는 쿠폰입니다."),
    COUPON_MINIMUM_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST,"COUPON_MINIMUM_AMOUNT_NOT_MET","쿠폰 적용 최소 주문 금액을 충족해야합니다."),
    COUPON_MAXIMUM_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST,"COUPON_MAXIMUM_AMOUNT_NOT_MET","쿠폰 적용 최대 주문 금액을 초과 하였습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
