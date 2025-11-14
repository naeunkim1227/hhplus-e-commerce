package io.hhplus.ecommerce.product.domain.exception;

import io.hhplus.ecommerce.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 상품 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // 404 Not Found
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "재고 선점 정보를 찾을 수 없습니다"),

    // 400 Bad Request
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", "수량은 1 이상이어야 합니다"),
    PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PRODUCT_NOT_AVAILABLE", "구매 불가능한 상품입니다"),
    PRODUCT_INACTIVE(HttpStatus.BAD_REQUEST, "PRODUCT_INACTIVE", "판매 중지된 상품입니다"),
    PRODUCT_DISCONTINUED(HttpStatus.BAD_REQUEST, "PRODUCT_DISCONTINUED", "단종된 상품입니다"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "OUT_OF_STOCK", "품절된 상품입니다"),
    INVALID_MAX_QUANTITY(HttpStatus.BAD_REQUEST,"INVALID_QUANTITY","최대 구매 가능 수량을 초과하였습니다."),


    // 409 Conflict
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "재고가 부족합니다"),
    SEARCH_CONDITION_LIMIT(HttpStatus.CONFLICT, "SEARCH_CONDITION_LIMIT", "최대 검색기간을 초과하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}