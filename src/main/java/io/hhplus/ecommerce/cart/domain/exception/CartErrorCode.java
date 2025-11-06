package io.hhplus.ecommerce.cart.domain.exception;

import io.hhplus.ecommerce.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {

    //400
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_NOT_FOUND", "장바구니를 찾을 수 없습니다"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "장바구니 항목을 찾을 수 없습니다"),
    INVALID_CART_QUANTITY(HttpStatus.BAD_REQUEST, "INVALID_CART_QUANTITY", "수량은 1 이상이어야 합니다");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
