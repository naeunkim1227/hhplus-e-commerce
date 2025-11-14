package io.hhplus.ecommerce.order.domain.exception;


import io.hhplus.ecommerce.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    //400
    MIN_ORDER_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST,"MIN_ORDER_AMOUNT_NOT_MET","최소 주문 금액을 충족하지 못했습니다."),
    ORDER_NOT_FOUND(HttpStatus.BAD_REQUEST,"ORDER_NOT_FOUND","주문을 찾을 수 없습니다"),
    PAYMENT_FAIL(HttpStatus.BAD_REQUEST,"PAYMENT_FAIL","주문이 실패 했습니다."),
    CART_NOT_FOUND(HttpStatus.BAD_REQUEST,"CART_NOT_FOUND","장바구니에 상품이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
