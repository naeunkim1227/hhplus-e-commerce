package io.hhplus.ecommerce.product.presentation.dto.request;

import io.hhplus.ecommerce.product.application.dto.command.ProductCreateCommand;
import io.hhplus.ecommerce.product.application.dto.command.ProductPopularCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 검색 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPopularRequest {
    private Integer days;
    private Integer limit;

    public ProductPopularCommand toCommand() {
        return ProductPopularCommand.builder()
                .days(days)
                .limit(limit)
                .build();
    }
}