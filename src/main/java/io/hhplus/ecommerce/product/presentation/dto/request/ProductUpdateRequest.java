package io.hhplus.ecommerce.product.presentation.dto.request;

import io.hhplus.ecommerce.product.application.dto.command.ProductUpdateCommand;
import io.hhplus.ecommerce.product.domain.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "상품명은 필수입니다")
    private String name;

    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 0보다 커야 합니다")
    private BigDecimal price;

    @NotNull(message = "재고는 필수입니다")
    @Positive(message = "재고는 0보다 커야 합니다")
    private Long stock;

    private ProductStatus status;

    public ProductUpdateCommand toCommand(Long productId) {
        return ProductUpdateCommand.builder()
                .productId(productId)
                .name(name)
                .price(price)
                .stock(stock)
                .status(status)
                .build();
    }
}