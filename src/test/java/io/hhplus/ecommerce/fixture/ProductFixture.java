package io.hhplus.ecommerce.fixture;

import io.hhplus.ecommerce.product.domain.entity.Product;
import io.hhplus.ecommerce.product.domain.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductFixture {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * 기본 상품 (ACTIVE, 재고 충분)
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static Product defaultProduct() {
        return Product.builder()
                .name("프레첼")
                .price(new BigDecimal("129000"))
                .stock(150L)
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 재고 부족 상품
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static Product outOfStockProduct() {
        return Product.builder()
                .name("아메리카노")
                .price(new BigDecimal("4500"))
                .stock(0L)
                .status(ProductStatus.ACTIVE)
                .version(0L)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }

    /**
     * 판매 중지 상품
     * ID는 null로 설정하여 JPA가 자동 생성하도록 함
     */
    public static Product inactiveProduct() {
        return Product.builder()
                .name("에어팟")
                .price(new BigDecimal("350000"))
                .stock(200L)
                .status(ProductStatus.INACTIVE)
                .version(0L)
                .createdAt(DEFAULT_TIMESTAMP)
                .updatedAt(DEFAULT_TIMESTAMP)
                .build();
    }


}