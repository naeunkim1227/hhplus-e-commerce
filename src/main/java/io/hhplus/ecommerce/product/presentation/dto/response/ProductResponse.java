package io.hhplus.ecommerce.product.presentation.dto.response;

import io.hhplus.ecommerce.product.application.dto.result.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 응답 DTO (Presentation Layer)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long stock;
    private String status;

    //dto >> Response
    public static ProductResponse from(ProductDto dto) {
        return ProductResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .status(dto.getStatus())
                .build();
    }
}