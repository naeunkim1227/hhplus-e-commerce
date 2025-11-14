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
    INVALID_CART_QUANTITY(HttpStatus.BAD_REQUEST, "INVALID_CART_QUANTITY", "수량은 1 이상이어야 합니다"),
    CART_ITEM_NOW_ALLOWED(HttpStatus.BAD_REQUEST,"CART_ITEM_NOW_ALLOWED","본인 소유의 장바구니만 수정할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
