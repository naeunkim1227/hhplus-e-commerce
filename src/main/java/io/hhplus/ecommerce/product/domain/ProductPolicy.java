package io.hhplus.ecommerce.product.domain;

import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public final class ProductPolicy {
    //기본값
    public static final ProductStatus DEFAULT_STATUS = ProductStatus.ACTIVE;
    public static final Long DEFAULT_STOCK = 0L;

    //최대 최소 재고
    public static final Long MAX_STOCK = 999L;
    public static final Long MIN_STOCK = 0L;

    //최대 최소 가격
    public static final BigDecimal MAX_PRICE = new BigDecimal(999999999);
    public static final BigDecimal MIN_PRICE = new BigDecimal(100);


    //재고 선점 시간
    public static final int RESERVATION_MINUTES = 10;


}
