package io.hhplus.ecommerce.user.domain.exception;

import io.hhplus.ecommerce.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    //400
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST,"USER_NOT_FOUND","찾을 수 없는 유저입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST,"INSUFFICIENT_BALANCE","잔액이 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
