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
    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST,"COUPON_ALREADY_ISSUED","이미 발급받은 쿠폰입니다"),
    COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST,"COUPON_SOLD_OUT","쿠폰이 모두 소진되었습니다"),
    COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST,"COUPON_NOT_AVAILABLE","사용할 수 없는 쿠폰입니다."),
    COUPON_MINIMUM_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST,"COUPON_MINIMUM_AMOUNT_NOT_MET","쿠폰 적용 최소 주문 금액을 충족해야합니다."),
    COUPON_MAXIMUM_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST,"COUPON_MAXIMUM_AMOUNT_NOT_MET","쿠폰 적용 최대 주문 금액을 초과 하였습니다"),
    USER_COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST,"USER_COUPON_NOT_FOUND","유저가 보유하지 않은 쿠폰입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
