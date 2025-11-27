package io.hhplus.ecommerce.product.application.dto.command;

import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductUpdateCommand {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Long stock;
    private ProductStatus status;
}